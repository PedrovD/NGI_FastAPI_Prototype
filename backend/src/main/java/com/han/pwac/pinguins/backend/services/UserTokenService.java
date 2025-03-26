package com.han.pwac.pinguins.backend.services;

import com.han.pwac.pinguins.backend.domain.DTO.TeacherDto;
import com.han.pwac.pinguins.backend.domain.DTO.VerificationDto;
import com.han.pwac.pinguins.backend.domain.DTO.SupervisorDto;
import com.han.pwac.pinguins.backend.domain.User;
import com.han.pwac.pinguins.backend.domain.VerificationType;
import com.han.pwac.pinguins.backend.exceptions.GlobalException;
import com.han.pwac.pinguins.backend.exceptions.InternalException;
import com.han.pwac.pinguins.backend.exceptions.NotFoundException;
import com.han.pwac.pinguins.backend.repository.SupervisorDao;
import com.han.pwac.pinguins.backend.repository.TeacherDao;
import com.han.pwac.pinguins.backend.repository.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserTokenService {

    private final UserDao userDao;
    private final SupervisorDao supervisorDAO;
    private final ProjectService projectService;
    private final TeacherDao teacherDao;

    @Autowired
    public UserTokenService(UserDao userDao, SupervisorDao supervisorDAO, ProjectService projectService, TeacherDao teacherDao) {
        this.supervisorDAO = supervisorDAO;
        this.projectService = projectService;
        this.userDao = userDao;
        this.teacherDao = teacherDao;
    }

    public boolean checkIfProviderIdMatchesBusinessId(String providerId, int projectId) {
        Optional<String> tokenOptional = Optional.ofNullable(providerId);
        if (tokenOptional.isEmpty()) {
            throw new NotFoundException("Cookie not found");
        }
        VerificationDto verification = getVerificationByProviderId(tokenOptional);

        if (verification.getBusinessId().isEmpty()) {
            throw new NotFoundException("Business does not align with current user");
        }
        return verification.getBusinessId().get() == projectService.getProject(projectId).business().businessId();
    }

    public VerificationDto getVerificationByProviderId(Optional<String> providerId) {
        if (providerId.isEmpty() || providerId.get().isEmpty()) {
            return new VerificationDto(VerificationType.NONE, null, null);
        }

        Optional<User> user = userDao.getByProviderId(providerId.get());
        if (user.isEmpty()) {
            return new VerificationDto(VerificationType.NONE, null, null);
        }

        if (user.get().getEmail() == null) {
            return new VerificationDto(VerificationType.INVALID, user.get().getId(), null);
        }

        Optional<SupervisorDto> supervisor = supervisorDAO.findById(user.get().getId());
        Optional<TeacherDto> teacher = teacherDao.findById(user.get().getId());
        if (supervisor.isPresent()) {
            return new VerificationDto(VerificationType.SUPERVISOR, user.get().getId(), supervisor.get().getBusinessId());
        } else if (teacher.isPresent()){
            return new VerificationDto(VerificationType.TEACHER, user.get().getId(), null);
        } else {
            return new VerificationDto(VerificationType.STUDENT, user.get().getId(), null);
        }
    }

    public void setEmail(int userId, String email) {
        if (!userDao.setEmail(userId, email)) {
            throw new InternalException("Er is een fout ontstaan bij het opslaan van het email.");
        }
    }
}