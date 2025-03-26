package com.han.pwac.pinguins.backend.services;

import com.han.pwac.pinguins.backend.domain.DTO.GetSkillDto;
import com.han.pwac.pinguins.backend.domain.DTO.StudentRepositoryDto;
import com.han.pwac.pinguins.backend.domain.EmailPart;
import com.han.pwac.pinguins.backend.exceptions.DuplicateValueException;
import com.han.pwac.pinguins.backend.exceptions.InvalidDataException;
import com.han.pwac.pinguins.backend.exceptions.NotFoundException;
import com.han.pwac.pinguins.backend.repository.SkillDao;
import com.han.pwac.pinguins.backend.repository.contract.ICronJobDao;
import com.han.pwac.pinguins.backend.services.contract.IBaseService;
import com.han.pwac.pinguins.backend.services.contract.IMailCronService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Stream;

@Service
public class SkillService implements IMailCronService {

    private final SkillDao skillDao;
    private final IBaseService<StudentRepositoryDto> studentService;
    private final ICronJobDao cronJobDao;

    public SkillService(SkillDao skillDao, IBaseService<StudentRepositoryDto> studentService, ICronJobDao cronJobDao) {
        this.skillDao = skillDao;
        this.studentService = studentService;
        this.cronJobDao = cronJobDao;
    }

    public Collection<GetSkillDto> getAllSkills() {
        return skillDao.getAll();
    }

    @Transactional
    public void addSkillToTask(Collection<Integer> newSkills, Integer taskId) {
        if (newSkills == null) throw new NotFoundException("Geen skill gekozen");
        if (taskId == null) throw new NotFoundException("Geen task gevonden");

        skillDao.removeSkillsFromTask(taskId);
        newSkills.forEach(skillId -> {
            if (!skillDao.existsTaskSkill(taskId, skillId)) {
                skillDao.addTaskSkill(taskId, skillId);
            }
        });
    }

    public GetSkillDto createSkill(String skillName) {
        String formattedSkillName = skillName.trim().replaceAll("\\s+", " ");

        Collection<GetSkillDto> skills = Optional.ofNullable(skillDao.getAll()).orElse(Collections.emptyList());
        if (skills.stream().anyMatch(skill -> skill.name().equalsIgnoreCase(formattedSkillName))) {
            throw new DuplicateValueException("Deze skill bestaat al");
        }
        skillDao.add(new GetSkillDto(0, formattedSkillName, true));

        int newId = skillDao.getLastInsertedId();
        if (newId == 0) {
            throw new NotFoundException("Er ging iets mis bij het aanmaken van de skill");
        }
        return new GetSkillDto(newId, formattedSkillName, true);
    }

    public void updateSkillName(int skillId, String skillName) {
        if (skillId <= 0) {
            throw new InvalidDataException("Ongeldige skillId");
        }

        if (skillDao.findById(skillId).isEmpty()) {
            throw new NotFoundException("De geselecteerde skill is niet gevonden");
        }

        String formattedSkillName = skillName.trim().replaceAll("\\s+", " ");
        if (formattedSkillName.length() > 50) {
            throw new InvalidDataException("De naam van de skill mag maximaal 50 karakters bevatten");
        }

        Collection<GetSkillDto> skills = Optional.ofNullable(skillDao.getAll()).orElse(Collections.emptyList());
        if (skills.stream().anyMatch(s -> s.name().equalsIgnoreCase(formattedSkillName) && s.skillId() != skillId)) {
            throw new DuplicateValueException("Er bestaat al een andere skill met deze naam");
        }

        skillDao.updateSkillName(skillId, formattedSkillName);
    }

    public void updateSkillAcceptance(int skillId, boolean isAccepted) {
        if (skillId <= 0) {
            throw new InvalidDataException("Ongeldige skillId");
        }

        if (skillDao.findById(skillId).isEmpty()) {
            throw new NotFoundException("De geselecteerde skill is niet gevonden");
        }

        if (isAccepted) {
            skillDao.acceptSkill(skillId);
        } else {
            skillDao.deleteSkillFromAllTasks(skillId);
            skillDao.deleteSkillFromAllStudents(skillId);
            skillDao.deleteSkill(skillId);
        }
    }

    @Override
    public Map<Integer, EmailPart> getBatchedMails() {
        Timestamp lastBatchedDate = cronJobDao.getPreviousRunDate();
        Collection<GetSkillDto> newSkillsSinceLastCronJob = skillDao.getAllSkillsCreatedAfter(lastBatchedDate);

        if (newSkillsSinceLastCronJob.isEmpty()) {
            return Collections.emptyMap();
        }

        Collection<StudentRepositoryDto> students = studentService.getAll();

        EmailPart part = new EmailPart("main");

        part.addHeading("Nieuwe skills die zijn toegevoegd:", EmailPart.HeadingSize.H3);

        Stream<String> skillNames = newSkillsSinceLastCronJob.stream().map(GetSkillDto::name);
        part.addList(skillNames.iterator());

        HashMap<Integer, EmailPart> map = new HashMap<>(students.size());

        for (StudentRepositoryDto student : students) {
            map.put(student.userId(), part);
        }

        return map;
    }
}
