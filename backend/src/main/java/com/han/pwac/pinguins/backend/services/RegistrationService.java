package com.han.pwac.pinguins.backend.services;

import com.han.pwac.pinguins.backend.domain.DTO.*;
import com.han.pwac.pinguins.backend.domain.EmailPart;
import com.han.pwac.pinguins.backend.domain.RegistrationId;
import com.han.pwac.pinguins.backend.domain.DTO.RegistrationDto;
import com.han.pwac.pinguins.backend.domain.StudentEmailSelection;
import com.han.pwac.pinguins.backend.repository.BusinessDao;
import com.han.pwac.pinguins.backend.repository.contract.IBusinessDao;
import com.han.pwac.pinguins.backend.repository.contract.ICronJobDao;
import com.han.pwac.pinguins.backend.repository.contract.IRegistrationDao;
import com.han.pwac.pinguins.backend.services.contract.IBaseService;
import com.han.pwac.pinguins.backend.services.contract.IMailCronService;
import com.han.pwac.pinguins.backend.services.contract.IRegistrationService;
import com.han.pwac.pinguins.backend.services.contract.IStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.han.pwac.pinguins.backend.services.contract.base.BaseService;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Stream;

@Service
public class RegistrationService extends BaseService<RegistrationDto, RegistrationId> implements IRegistrationService, IMailCronService {
    IStudentService studentService;
    private final TaskService taskService;
    private final IRegistrationDao registrationDao;
    private final ICronJobDao cronJobDao;

    @Value("${frontend.url}")
    private String frontEndUrl;

    @Autowired
    public RegistrationService(IRegistrationDao registrationDao, IStudentService studentService, TaskService taskService, ICronJobDao cronJobDao) {
        super(registrationDao);
        this.registrationDao = registrationDao;
        this.studentService = studentService;
        this.taskService = taskService;
        this.cronJobDao = cronJobDao;
    }

    public Collection<GetRegistrationDto> getAllRegistrationsForTask(int taskId) {
        Stream<RegistrationDto> allRegistrations = registrationDao.getAll().stream().filter(registration -> registration.taskId() == taskId && registration.accepted().isEmpty());

        return allRegistrations.map(registration -> new GetRegistrationDto(
                registration.taskId(),
                registration.description(),
                registration.accepted(),
                registration.response(),
                studentService.getStudentById(registration.userId()) // registration should always have a student linked to it
        )).toList();
    }

    public boolean addRegistration(int taskId, int userId, String reason){
        RegistrationId registrationId = new RegistrationId(taskId, userId);
        if (registrationDao.findById(registrationId).isPresent()) {
            return false;
        }

        RegistrationDto registration = new RegistrationDto(0, taskId, userId, reason, Optional.empty(), "");
        if (!registration.isValid()) {
            return false;
        }
        return registrationDao.add(registration);
    }

    public Collection<Integer> getRegistrationsForUser(int userId){
        return registrationDao.getRegistrationsForUser(userId);
    }

    @Override
    public Map<Integer, EmailPart> getBatchedMails() {
        Timestamp lastRunDate = cronJobDao.getPreviousRunDate();
        Collection<RegistrationDto> registrations =  registrationDao.getRegistrationsAfter(lastRunDate);

        if (registrations.isEmpty()) {
            return Collections.emptyMap();
        }

        HashMap<Integer, List<RegistrationDto>> mapped = registrations.stream().reduce(new HashMap<>(32), (accumulator, element) -> {
            List<RegistrationDto> parts = accumulator.computeIfAbsent(element.supervisorId(), k -> new ArrayList<>(2));
            parts.add(element);
            return accumulator;
        }, (accumulator, element) -> {
            element.forEach((key, value) -> {
                List<RegistrationDto> parts = accumulator.get(key);
                parts.addAll(value);
            });
            return accumulator;
        });

        HashMap<Integer, EmailPart> parts = new HashMap<>(mapped.size());
        mapped.forEach((key, value) -> {
            EmailPart part = new EmailPart("main");

            part.addHeading("Nieuwe aanmeldingen:", EmailPart.HeadingSize.H3);

            part.addList(value.stream().map(registration -> {
                String studentUsername = studentService.getStudentById(registration.userId()).username();
                Optional<TaskDto> task = taskService.findById(registration.taskId());
                if (task.isEmpty()) {
                    return studentUsername;
                }

                String taskName = task.get().getTitle();
                int projectId = task.get().getProjectId();
                return studentUsername + " heeft aangemeld op <a href=\"" + frontEndUrl + "/projects/" + projectId + "#task-" + task.get().getTaskId() + "\">" + taskName + "</a>";
            }).iterator());

            parts.put(key, part);
        });

        return parts;
    }

    @Override
    public Collection<String> getEmailAddressesForRegistrations(int selection, int taskId) {
         ArrayList<Boolean> validStates = new ArrayList<>(3);
         if ((selection & StudentEmailSelection.ACCEPTED.getFlagValue()) != 0) {
             validStates.add(true);
         }
         if ((selection & StudentEmailSelection.REGISTERED.getFlagValue()) != 0) {
             validStates.add(null);
         }
         if ((selection & StudentEmailSelection.REJECTED.getFlagValue()) != 0) {
             validStates.add(false);
         }

         return dao.getAll().stream()
                 .filter(registration -> registration.taskId() == taskId)
                 .filter(registration -> validStates.contains(registration.accepted().orElse(null)))
                 .map(registration -> studentService.getStudentById(registration.userId()).email())
                 .toList();
    }
}
