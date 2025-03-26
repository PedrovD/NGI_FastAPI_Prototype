package com.han.pwac.pinguins.backend.services;

import com.han.pwac.pinguins.backend.domain.DTO.*;
import com.han.pwac.pinguins.backend.domain.EmailPart;
import com.han.pwac.pinguins.backend.exceptions.InvalidDataException;
import com.han.pwac.pinguins.backend.exceptions.NotFoundException;
import com.han.pwac.pinguins.backend.exceptions.ProjectInvalidBodyException;
import com.han.pwac.pinguins.backend.exceptions.TaskInvalidBodyException;
import com.han.pwac.pinguins.backend.repository.ProjectDao;
import com.han.pwac.pinguins.backend.repository.RegistrationsDao;
import com.han.pwac.pinguins.backend.repository.SkillDao;
import com.han.pwac.pinguins.backend.repository.TaskDao;
import com.han.pwac.pinguins.backend.repository.contract.ICronJobDao;
import com.han.pwac.pinguins.backend.services.contract.IBaseService;
import com.han.pwac.pinguins.backend.services.contract.IMailCronService;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

@Service
public class TaskService implements IMailCronService {
    private final TaskDao taskDAO;
    private final SkillDao skillDAO;
    private final ProjectDao projectDAO;
    private final RegistrationsDao registrationsDao;
    private final IBaseService<StudentRepositoryDto> studentService;
    private final ICronJobDao cronJobDao;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Autowired
    public TaskService(TaskDao taskDAO, ProjectDao projectDAO, SkillDao skillDAO, RegistrationsDao registrationsDao, IBaseService<StudentRepositoryDto> studentService, ICronJobDao cronJobDao) {
        this.taskDAO = taskDAO;
        this.projectDAO = projectDAO;
        this.skillDAO = skillDAO;
        this.registrationsDao = registrationsDao;
        this.studentService = studentService;
        this.cronJobDao = cronJobDao;
    }

    public void addTaskToProject(int projectId, TaskDto task) {

        if (projectId < 0 || !projectDAO.checkIfProjectExists(projectId).get()) {
            throw new NotFoundException("Project bestaat niet");
        }
        if (taskDAO.findById(task.getTaskId()).isPresent()) {
            throw new TaskInvalidBodyException("Task bestaat al");
        }

        if (task.getTitle() == null || task.getTitle().isEmpty()) {
            throw new TaskInvalidBodyException("Titel mag niet leeg zijn");
        }

        if (task.getTitle().length() > 50) {
            throw new TaskInvalidBodyException("Titel mag niet langer zijn dan 50 karakters");
        }

        if (task.getDescription() == null || task.getDescription().isEmpty()) {
            throw new TaskInvalidBodyException("Beschrijving mag niet leeg zijn");
        }

        if (IValidate.stripMarkdownFromString(task.getDescription()).length() > 4000) {
            throw new TaskInvalidBodyException("Beschrijving mag niet langer zijn dan 4000 karakters");
        }

        if (taskDAO.getByProjectId(projectId).stream().anyMatch(t -> t.getTitle().equals(task.getTitle()))) {
            throw new TaskInvalidBodyException("Task naam is al in gebruik");
        }
        taskDAO.addTask(task);
    }

    public List<TaskWithSkills> getTasksByProjectId(int projectId) {
        Optional<Boolean> projectExists = projectDAO.checkIfProjectExists(projectId);
        if (projectId <= 0 || (projectExists.isPresent() && !projectExists.get())) {
            throw new NotFoundException("Project bestaat niet");
        }

        List<TaskDto> tasks = taskDAO.getTasksByProjectId(projectId);

        List<TaskWithSkills> tasksWithSkills = new ArrayList<>();
        Collection<RegistrationDto> allRegistrations = registrationsDao.getAll();
        for (TaskDto task : tasks) {
            int totalRegistered = allRegistrations.stream().filter(registration -> registration.taskId() == task.getTaskId() && registration.accepted().isEmpty()).toArray().length;
            int totalAccepted = allRegistrations.stream().filter(registration -> registration.taskId() == task.getTaskId() && registration.accepted().orElse(false)).toArray().length;
            tasksWithSkills.add(new TaskWithSkills(task, totalAccepted, totalRegistered, skillDAO.getTaskSkills(task.getTaskId())));
        }

        return tasksWithSkills;
    }

    public Optional<TaskDto> findById(int id) {
        return taskDAO.findById(id);
    }

    public int getProjectIdFromTasks(int taskId) {
        return taskDAO.GetProjectIdFromTask(taskId)
                .orElseThrow(() -> new NotFoundException("Project ID niet gevonden voor: " + taskId));
    }

    @Override
    public Map<Integer, EmailPart> getBatchedMails() {
        Timestamp lastRunDate = cronJobDao.getPreviousRunDate();
        Collection<TaskDto> tasks = taskDAO.getAllTasksAfter(lastRunDate);

        if (tasks.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Pair<Collection<GetSkillDto>, TaskDto>> taskWithSkills = tasks.stream().map(task -> new Pair<>((Collection<GetSkillDto>)skillDAO.getTaskSkills(task.getTaskId()), task)).toList();
        Collection<StudentRepositoryDto> students = studentService.getAll();

        HashMap<Integer, EmailPart> parts = new HashMap<>(students.size());
        for (StudentRepositoryDto student : students) {

            Collection<GetSkillWithDescriptionDto> skills = skillDAO.getAllForStudent(student.userId());

            List<TaskDto> foundTasks = taskWithSkills.stream()
                    .filter(task -> skills.stream().anyMatch(s -> task.a.contains(s.skill())))
                    .map(task -> task.b)
                    .toList();

            if (foundTasks.isEmpty()) {
                continue;
            }

            EmailPart part = new EmailPart("main");

            part.addHeading("Nieuwe taken die voor jou toepasselijk zijn:", EmailPart.HeadingSize.H3);
            part.addList(foundTasks.stream().map(task -> {
                int projectId = task.getProjectId();
                return "<a href=\"" + frontendUrl + "/projects/" + projectId + "#task-" + task.getTaskId() + "\">" + task.getTitle() + "</a>";
            }).iterator());

            parts.put(student.userId(), part);
        }

        return parts;
    }
}
