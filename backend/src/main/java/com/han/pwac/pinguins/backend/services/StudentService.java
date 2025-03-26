package com.han.pwac.pinguins.backend.services;

import com.han.pwac.pinguins.backend.domain.DTO.GetSkillDto;
import com.han.pwac.pinguins.backend.domain.DTO.GetSkillWithDescriptionDto;
import com.han.pwac.pinguins.backend.domain.DTO.StudentDto;
import com.han.pwac.pinguins.backend.domain.DTO.StudentRepositoryDto;
import com.han.pwac.pinguins.backend.exceptions.NotFoundException;
import com.han.pwac.pinguins.backend.exceptions.StudentNotFoundException;
import com.han.pwac.pinguins.backend.repository.SkillDao;
import com.han.pwac.pinguins.backend.repository.StudentDao;
import com.han.pwac.pinguins.backend.repository.contract.IBaseDao;
import com.han.pwac.pinguins.backend.repository.contract.ISkillDao;
import com.han.pwac.pinguins.backend.services.contract.IStudentService;
import jakarta.transaction.Transactional;
import org.hibernate.annotations.NotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
public class StudentService extends BaseService<StudentRepositoryDto> implements IStudentService {
    private final StudentDao studentDao;
    private final ISkillDao skillDao;

    @Autowired
    public StudentService(ISkillDao skillDao, StudentDao studentDao) {
        super(studentDao);
        this.skillDao = skillDao;
        this.studentDao = studentDao;
    }

    public StudentDto getStudentById(int studentId) {
        Optional<StudentRepositoryDto> student = studentDao.findById(studentId);
        if (student.isEmpty()) {
            throw new StudentNotFoundException("Student bestaat niet");
        }

        return new StudentDto(
                student.get().userId(),
                student.get().username(),
                student.get().description(),
                student.get().profilePicture(),
                student.get().email(),
                student.get().cv(),
                skillDao.getAllForStudent(studentId)
        );
    }

    public void editStudentSkillDescription(Integer userId, GetSkillWithDescriptionDto skillWithDescriptionDto) {
        if (skillDao.getAllForStudent(userId).stream().noneMatch(s -> s.skill().skillId() == skillWithDescriptionDto.skill().skillId())) {
            throw new NotFoundException("Kan de skill niet aanpassen want deze is niet gevonden.");
        }
        skillDao.editSkillDescription(userId, skillWithDescriptionDto);
    }

    @Transactional
    public void updateStudentSkills(Integer userId, Collection<Integer> newSkills) {
        Collection<Integer> currentSkills = skillDao.getAllForStudent(userId).stream().map(GetSkillWithDescriptionDto::skill).map(GetSkillDto::skillId).toList();

        Collection<Integer> removedSkills = currentSkills.stream().filter(skill -> !newSkills.contains(skill)).toList();
        Collection<Integer> addedSkills = newSkills.stream().filter(skill -> !currentSkills.contains(skill)).toList();

        removedSkills.forEach(skillId -> skillDao.removeSkillFromStudent(userId, skillId));
        addedSkills.forEach(skillId -> {
            skillDao.findById(skillId).orElseThrow(() -> new NotFoundException("Skill met id " + skillId + " niet gevonden"));
            skillDao.addSkillToStudent(userId, skillId);
        });
    }
}

