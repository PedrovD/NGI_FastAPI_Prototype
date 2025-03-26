package com.han.pwac.pinguins.backend.controller;

import com.han.pwac.pinguins.backend.annotations.OAuth2;
import com.han.pwac.pinguins.backend.annotations.Sanitation;
import com.han.pwac.pinguins.backend.domain.DTO.*;
import com.han.pwac.pinguins.backend.domain.UserInfo;
import com.han.pwac.pinguins.backend.domain.VerificationType;
import com.han.pwac.pinguins.backend.exceptions.CannotViewOtherStudentsException;
import com.han.pwac.pinguins.backend.services.StudentService;
import com.han.pwac.pinguins.backend.services.TaskService;
import com.han.pwac.pinguins.backend.services.UserTokenService;
import com.han.pwac.pinguins.backend.services.contract.IFileService;
import com.han.pwac.pinguins.backend.services.contract.IProjectService;
import com.han.pwac.pinguins.backend.services.contract.IRegistrationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/students")
public class StudentController {
    private final UserTokenService userTokenService;
    private final StudentService studentService;
    private final IRegistrationService registrationService;
    private final IFileService fileService;
    private final TaskService taskService;
    private final IProjectService projectService;

    @Autowired
    public StudentController(UserTokenService userTokenService, StudentService studentService, IRegistrationService registrationService, IFileService fileService, TaskService taskService, IProjectService projectService) {
        this.userTokenService = userTokenService;
        this.studentService = studentService;
        this.registrationService = registrationService;
        this.fileService = fileService;
        this.taskService = taskService;
        this.projectService = projectService;
    }

    @OAuth2
    @GetMapping("{studentId}")
    public StudentDto getStudentById(@PathVariable int studentId, UserInfo userInfo) {
        VerificationDto getVerificationByToken = userTokenService.getVerificationByProviderId(Optional.of(userInfo.id()));

        if (getVerificationByToken.getType() == VerificationType.STUDENT && getVerificationByToken.getUserId().get() != studentId) {
            throw new CannotViewOtherStudentsException("Je bent niet geautoriseerd om deze student te bekijken");
        }

        return studentService.getStudentById(studentId);
    }

    @OAuth2
    @Sanitation
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateStudent(@RequestParam String description,
                                           @RequestParam(required = false) Optional<MultipartFile> profilePicture,
                                           @RequestParam(required = false) Optional<MultipartFile> cv,
                                           UserInfo userInfo) {
        // should always be found as user must be logged in
        int studentId = userTokenService.getVerificationByProviderId(Optional.of(userInfo.id())).getUserId().get();

        Optional<StudentRepositoryDto> optionalStudentDto = studentService.findById(studentId);
        if (optionalStudentDto.isEmpty()) {
            return ResponseEntity.status(HttpStatusCode.valueOf(403)).build();
        }
        StudentRepositoryDto studentDto = optionalStudentDto.get();

        Optional<String> profilePicturePath = profilePicture.map(file ->
                        fileService.uploadFile(file, new MimeType("image", "*")))
                .or(() -> studentDto.profilePicture().path());

        Optional<String> cvPath = cv.map(file ->
                        fileService.uploadFile(file, new MimeType("application", "pdf")))
                .or(() -> studentDto.cv().path());

        StudentRepositoryDto student = new StudentRepositoryDto(
                studentId,
                studentDto.providerId(),
                studentDto.username(),
                description,
                new FileDto(profilePicturePath),
                new FileDto(cvPath),
                studentDto.email()
        );

        if (!studentService.update(studentId, student)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @OAuth2
    @PutMapping("/skill")
    public ResponseEntity<?> editStudentSkillDescription(@Valid @RequestBody GetSkillWithDescriptionDto skillWithDescriptionDto, UserInfo userInfo) {
        VerificationDto verification = userTokenService.getVerificationByProviderId(Optional.ofNullable(userInfo.id()));
        studentService.editStudentSkillDescription(verification.getUserId().get(), skillWithDescriptionDto);
        return ResponseEntity.ok().build();
    }

    @OAuth2
    @PutMapping("/skills")
    public ResponseEntity<?> updateStudentSkills(@Valid @RequestBody Collection<Integer> newSkills,
                                                 UserInfo userInfo) {
        VerificationDto verification = userTokenService.getVerificationByProviderId(Optional.ofNullable(userInfo.id()));
        studentService.updateStudentSkills(verification.getUserId().get(), newSkills);
        return ResponseEntity.ok().build();
    }

    @OAuth2
    @GetMapping("/email")
    public ResponseEntity<Collection<String>> getStudentEmails(@Max(7) @RequestParam int selection, @RequestParam int taskId, UserInfo userInfo) {
        VerificationDto verification = userTokenService.getVerificationByProviderId(Optional.ofNullable(userInfo.id()));

        // user should be a supervisor here as stated in the security config
        int businessId = verification.getBusinessId().get();

        Optional<TaskDto> task = taskService.findById(taskId);
        if (task.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        int taskBusinessId = projectService.getProject(task.get().getProjectId()).business().businessId();
        if (businessId != taskBusinessId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(registrationService.getEmailAddressesForRegistrations(selection, taskId));
    }
}
