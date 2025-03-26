package com.han.pwac.pinguins.backend.controller;

import com.han.pwac.pinguins.backend.annotations.OAuth2;
import com.han.pwac.pinguins.backend.annotations.Sanitation;
import com.han.pwac.pinguins.backend.domain.*;
import com.han.pwac.pinguins.backend.domain.DTO.*;

import com.han.pwac.pinguins.backend.services.BusinessService;
import com.han.pwac.pinguins.backend.services.ProjectService;
import com.han.pwac.pinguins.backend.services.TaskService;
import com.han.pwac.pinguins.backend.services.UserTokenService;
import com.han.pwac.pinguins.backend.services.contract.IBaseService;
import com.han.pwac.pinguins.backend.services.contract.IMailService;
import com.han.pwac.pinguins.backend.services.contract.IRegistrationService;
import com.han.pwac.pinguins.backend.services.contract.IStudentService;
import jakarta.validation.Valid;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/registrations")
public class RegistrationController {
    private final IRegistrationService registrationService;
    private final UserTokenService userTokenService;
    private final IMailService mailService;
    private final IBaseService<StudentRepositoryDto> studentService;
    private final BusinessService businessService;
    private final TaskService taskService;


    @Value("${frontend.url}")
    public String frontendUrl;

    @Autowired
    public RegistrationController(IRegistrationService registrationService, UserTokenService userTokenService, IMailService mailService, IStudentService studentService, BusinessService businessService, TaskService taskService) {
        this.registrationService = registrationService;
        this.userTokenService = userTokenService;
        this.mailService = mailService;
        this.studentService = studentService;
        this.businessService = businessService;
        this.taskService = taskService;
    }

    @GetMapping("/{taskId}")
    public Collection<GetRegistrationDto> getRegistrations(@PathVariable int taskId) {
        return registrationService.getAllRegistrationsForTask(taskId);
    }

    @OAuth2
    @Sanitation
    @PostMapping("/{taskId}")
    public ResponseEntity<?> createRegistration(@PathVariable int taskId,
                                                @RequestBody String reason,
                                                UserInfo userInfo) throws AuthenticationException {

        VerificationDto verification = userTokenService.getVerificationByProviderId(Optional.of(userInfo.id()));
        if (verification.getType() != VerificationType.STUDENT) {
            throw new AuthenticationException("U bent geen student.");
        }

        if (registrationService.addRegistration(taskId, verification.getUserId().get(), reason)) {
            return new ResponseEntity<>("Registratie succesvol", HttpStatus.CREATED);
        }
        return new ResponseEntity<>("Registratie mislukt.", HttpStatus.BAD_REQUEST);
    }

    @OAuth2
    @Sanitation
    @PatchMapping
    public ResponseEntity<Object> updateRegistration(@Valid @RequestBody PatchRegistrationDto editRegistrationDto, UserInfo userInfo) {
        RegistrationId id = new RegistrationId(editRegistrationDto.userId(), editRegistrationDto.taskId());
        Optional<RegistrationDto> optional = registrationService.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        VerificationDto verificationDTO = userTokenService.getVerificationByProviderId(Optional.of(userInfo.id()));
        Collection<SupervisorDto> supervisors = businessService.getAllSupervisors(verificationDTO.getBusinessId().get());
        boolean isAuthorized = supervisors.stream()
                .anyMatch(supervisor -> Integer.valueOf(supervisor.getSupervisorId()).equals(verificationDTO.getUserId().orElse(null)));
        if (
                verificationDTO.getUserId().isEmpty() ||
                        !isAuthorized
        ) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        RegistrationDto registration = new RegistrationDto(
                optional.get().supervisorId(),
                optional.get().taskId(),
                optional.get().userId(),
                optional.get().description(),
                Optional.of(editRegistrationDto.accepted()),
                editRegistrationDto.response()
        );

        if (registrationService.update(id, registration)) {
            EmailBody body = new EmailBody(frontendUrl);
            Optional<EmailPart> part = body.getPart("main");

            Optional<StudentRepositoryDto> student = studentService.findById(registration.userId());
            assert student.isPresent(); // Student is expected to be present as it is not null in the database

            String studentEmail = student.get().email();

            if (part.isPresent()) {
                String studentName = student.get().username();
                part.get().addHeading("Beste " + studentName, EmailPart.HeadingSize.H1);

                String status = "<span style='color: red; font-weight: 600;'>afgewezen</span>";
                if (editRegistrationDto.accepted()) {
                    status = "<span style='color: green; font-weight: 600;'>geaccepteerd</span>";
                }
                String message = "U bent " + status + " bij de taak <span style='font-style: italic;'>" + taskService.findById(registration.taskId()).get().getTitle() + "</span>.";
                if (!editRegistrationDto.response().isEmpty()) {
                    message += "<br>De opdrachtgever heeft een toelichting meegegeven: " + editRegistrationDto.response();
                }

                part.get().addMessage(message);
                part.get().addLinkButton("Naar de taak", frontendUrl + "/projects/" + verificationDTO.getBusinessId().get() + "#task-" + registration.taskId());
            }

            Mail mail = new Mail("Aanmelding", studentEmail, body);

            mailService.sendMail(mail);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @OAuth2
    @GetMapping("/existing-user-registrations")
    public ResponseEntity<Collection<Integer>> getRegistrationsForUser(UserInfo userInfo)
            throws AuthenticationException {
        VerificationDto verification = userTokenService.getVerificationByProviderId(Optional.of(userInfo.id()));
        if (verification.getType() != VerificationType.STUDENT) {
            throw new AuthenticationException("U bent geen student.");
        }

        Collection<Integer> registeredTaskIds = registrationService.getRegistrationsForUser(verification.getUserId().get());
        return ResponseEntity.ok(registeredTaskIds);
    }
}
