package com.han.pwac.pinguins.backend.controller;

import com.han.pwac.pinguins.backend.authentication.Provider;
import com.han.pwac.pinguins.backend.domain.DTO.*;
import com.han.pwac.pinguins.backend.domain.UserInfo;
import com.han.pwac.pinguins.backend.domain.VerificationType;
import com.han.pwac.pinguins.backend.exceptions.CannotViewOtherStudentsException;
import com.han.pwac.pinguins.backend.exceptions.StudentNotFoundException;
import com.han.pwac.pinguins.backend.services.StudentService;
import com.han.pwac.pinguins.backend.services.TaskService;
import com.han.pwac.pinguins.backend.services.UserTokenService;
import com.han.pwac.pinguins.backend.repository.StudentDao;
import com.han.pwac.pinguins.backend.repository.contract.ISkillDao;
import com.han.pwac.pinguins.backend.repository.contract.IBaseDao;
import com.han.pwac.pinguins.backend.services.contract.IFileService;
import com.han.pwac.pinguins.backend.services.contract.IProjectService;
import com.han.pwac.pinguins.backend.services.contract.IRegistrationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StudentControllerTest {
    @InjectMocks
    private StudentController sut;
    @Mock
    private UserTokenService userTokenService;
    @Mock
    private StudentService studentService;
    @Mock
    private IFileService fileService;
    @Mock
    private MultipartFile imageFile;
    @Mock
    private MultipartFile pdfFile;
    @Mock
    private ISkillDao skillDao;
    @Mock
    private StudentDao studentDao;
    @Mock
    private IRegistrationService registrationService;

    @Mock
    private TaskService taskService;

    @Mock
    private IProjectService projectService;

    @Test
    public void test_updateStudent_Valid() {
        // arrange
        int studentId = 1;
        ArgumentCaptor<StudentRepositoryDto> captor = ArgumentCaptor.forClass(StudentRepositoryDto.class);

        when(userTokenService.getVerificationByProviderId(Optional.of("sessionId"))).thenReturn(new VerificationDto(VerificationType.STUDENT, 1, null));
        when(studentService.findById(studentId)).thenReturn(Optional.of(new StudentRepositoryDto(
                studentId,
                "sessionId",
                "name",
                "description",
                new FileDto(Optional.of("image.png")),
                new FileDto(Optional.of("cv.pdf")),
                "email@email.com"
        )));

        when(fileService.uploadFile(imageFile, new MimeType("image", "*"))).thenReturn("newImage.png");
        when(fileService.uploadFile(pdfFile, new MimeType("application", "pdf"))).thenReturn("newCv.pdf");

        when(studentService.update(eq(studentId), captor.capture())).thenReturn(true);

        // act
        ResponseEntity<?> response = sut.updateStudent("newDescription", Optional.of(imageFile), Optional.of(pdfFile), new UserInfo(Provider.GITHUB, "sessionId", null, null, null));

        // assert
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        verify(fileService, times(2)).uploadFile(any(MultipartFile.class), any(MimeType.class));
        verify(studentService, times(1)).update(eq(studentId), any(StudentRepositoryDto.class));

        StudentRepositoryDto capturedArg = captor.getValue();
        assertEquals(studentId, capturedArg.userId());
        assertEquals("newImage.png", capturedArg.profilePicture().path().get());
        assertEquals("newCv.pdf", capturedArg.cv().path().get());
        assertEquals("newDescription", capturedArg.description());
    }

    @Test
    public void test_updateStudent_OnlyProfilePictureChange() {
        // arrange
        int studentId = 1;
        ArgumentCaptor<StudentRepositoryDto> captor = ArgumentCaptor.forClass(StudentRepositoryDto.class);

        when(userTokenService.getVerificationByProviderId(Optional.of("sessionId"))).thenReturn(new VerificationDto(VerificationType.STUDENT, 1, null));
        when(studentService.findById(studentId)).thenReturn(Optional.of(new StudentRepositoryDto(
                studentId,
                "sessionId",
                "name",
                "description",
                new FileDto(Optional.of("image.png")),
                new FileDto(Optional.of("cv.pdf")),
                "email@email.com"
        )));

        when(fileService.uploadFile(pdfFile, new MimeType("application", "pdf"))).thenReturn("newCv.pdf");

        when(studentService.update(eq(studentId), captor.capture())).thenReturn(true);

        // act
        ResponseEntity<?> response = sut.updateStudent("newDescription", Optional.empty(), Optional.of(pdfFile), new UserInfo(Provider.GITHUB, "sessionId", null, null, null));

        // assert
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        verify(fileService, times(1)).uploadFile(any(MultipartFile.class), any(MimeType.class));
        verify(studentService, times(1)).update(eq(studentId), any(StudentRepositoryDto.class));

        StudentRepositoryDto capturedArg = captor.getValue();
        assertEquals(studentId, capturedArg.userId());
        assertEquals("image.png", capturedArg.profilePicture().path().get());
        assertEquals("newCv.pdf", capturedArg.cv().path().get());
        assertEquals("newDescription", capturedArg.description());
    }

    @Test
    public void test_updateStudent_OnlyCVChange() {
        // arrange
        int studentId = 1;
        ArgumentCaptor<StudentRepositoryDto> captor = ArgumentCaptor.forClass(StudentRepositoryDto.class);

        when(userTokenService.getVerificationByProviderId(Optional.of("sessionId"))).thenReturn(new VerificationDto(VerificationType.STUDENT, 1, null));
        when(studentService.findById(studentId)).thenReturn(Optional.of(new StudentRepositoryDto(
                studentId,
                "sessionId",
                "name",
                "description",
                new FileDto(Optional.of("image.png")),
                new FileDto(Optional.of("cv.pdf")),
                "email@email.com"
        )));

        when(fileService.uploadFile(imageFile, new MimeType("image", "*"))).thenReturn("newImage.png");

        when(studentService.update(eq(studentId), captor.capture())).thenReturn(true);

        // act
        ResponseEntity<?> response = sut.updateStudent("newDescription", Optional.of(imageFile), Optional.empty(), new UserInfo(Provider.GITHUB, "sessionId", null, null, null));

        // assert
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        verify(fileService, times(1)).uploadFile(any(MultipartFile.class), any(MimeType.class));
        verify(studentService, times(1)).update(eq(studentId), any(StudentRepositoryDto.class));

        StudentRepositoryDto capturedArg = captor.getValue();
        assertEquals(studentId, capturedArg.userId());
        assertEquals("newImage.png", capturedArg.profilePicture().path().get());
        assertEquals("cv.pdf", capturedArg.cv().path().get());
        assertEquals("newDescription", capturedArg.description());
    }

    @Test
    public void test_updateStudent_NoFileChanges() {
        // arrange
        int studentId = 1;
        ArgumentCaptor<StudentRepositoryDto> captor = ArgumentCaptor.forClass(StudentRepositoryDto.class);

        when(userTokenService.getVerificationByProviderId(Optional.of("sessionId"))).thenReturn(new VerificationDto(VerificationType.STUDENT, 1, null));
        when(studentService.findById(studentId)).thenReturn(Optional.of(new StudentRepositoryDto(
                studentId,
                "sessionId",
                "name",
                "description",
                new FileDto(Optional.of("image.png")),
                new FileDto(Optional.of("cv.pdf")),
                "email@email.com"
        )));

        when(studentService.update(eq(studentId), captor.capture())).thenReturn(true);

        // act
        ResponseEntity<?> response = sut.updateStudent("newDescription", Optional.empty(), Optional.empty(), new UserInfo(Provider.GITHUB, "sessionId", null, null, null));

        // assert
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        verify(fileService, times(0)).uploadFile(any(MultipartFile.class), any(MimeType.class));
        verify(studentService, times(1)).update(eq(studentId), any(StudentRepositoryDto.class));

        StudentRepositoryDto capturedArg = captor.getValue();
        assertEquals(studentId, capturedArg.userId());
        assertEquals("image.png", capturedArg.profilePicture().path().get());
        assertEquals("cv.pdf", capturedArg.cv().path().get());
        assertEquals("newDescription", capturedArg.description());
    }

    @Test
    public void test_updateStudent_NotAStudent() {
        // arrange
        when(userTokenService.getVerificationByProviderId(Optional.of("sessionId"))).thenReturn(new VerificationDto(VerificationType.SUPERVISOR, 2, 2));
        when(studentService.findById(2)).thenReturn(Optional.empty());

        // act
        ResponseEntity<?> response = sut.updateStudent("newDescription", Optional.of(imageFile), Optional.of(pdfFile), new UserInfo(Provider.GITHUB, "sessionId", null, null, null));

        // assert
        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    public void test_updateStudent_BadData() {
        // arrange
        int studentId = 1;

        IBaseDao<StudentRepositoryDto> baseDao = mock(IBaseDao.class);
        StudentService studentService = new StudentService(skillDao, studentDao);
        StudentController tempController = new StudentController(userTokenService, studentService, registrationService, fileService, taskService, projectService);

        when(userTokenService.getVerificationByProviderId(Optional.of("sessionId"))).thenReturn(new VerificationDto(VerificationType.STUDENT, 1, null));
        when(studentService.findById(studentId)).thenReturn(Optional.of(new StudentRepositoryDto(
                studentId,
                "sessionId",
                "name",
                "description",
                new FileDto(Optional.of("image.png")),
                new FileDto(Optional.of("cv.pdf")),
                "email@email.com"
        )));

        // act
        ResponseEntity<?> response = tempController.updateStudent("a".repeat(401), Optional.empty(), Optional.empty(), new UserInfo(Provider.GITHUB, "sessionId", null, null, null));

        // assert
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    public void getStudentById_successfulAttempt() {
        // Arrange
        StudentDto studentDto = new StudentDto(1, "abc", "def", new FileDto(Optional.of("ghi")), "email@email.com", new FileDto(Optional.of("jkl")), new ArrayList<>());
        when(studentService.getStudentById(anyInt())).thenReturn(studentDto);
        when(userTokenService.getVerificationByProviderId(Optional.of("abc"))).thenReturn(new VerificationDto(VerificationType.STUDENT, 1, null));

        // Act
        StudentDto response = sut.getStudentById(1, new UserInfo(Provider.GITHUB, "abc", null, null, null));

        // Assert
        assertEquals(studentDto, response);
    }

    @Test
    public void getStudentById_unautorized() {
        // Arrange
        StudentDto studentDto = new StudentDto(1, "abc", "def", new FileDto(Optional.of("ghi")), "email@email.com", new FileDto(Optional.of("jkl")), new ArrayList<>());
        when(userTokenService.getVerificationByProviderId(Optional.of("abc"))).thenReturn(new VerificationDto(VerificationType.STUDENT, 2, null));

        // Act + Assert
        Assertions.assertThrows(CannotViewOtherStudentsException.class,
                () -> sut.getStudentById(1, new UserInfo(Provider.GITHUB, "abc", null, null, null)));
    }

    @Test
    public void getStudentById_studentServiceReturnsException() {
        // Arrange
        when(studentService.getStudentById(anyInt())).thenThrow(StudentNotFoundException.class);
        when(userTokenService.getVerificationByProviderId(Optional.of("abc"))).thenReturn(new VerificationDto(VerificationType.STUDENT, 1, null));

        // Act + Assert
        assertThrows(
                StudentNotFoundException.class,
                () -> sut.getStudentById(1, new UserInfo(Provider.GITHUB, "abc", null, null, null))
        );
    }

    @Test
    public void test_editStudentSkillDescription_valid() {
        // Arrange
        GetSkillWithDescriptionDto skillWithDescriptionDto = new GetSkillWithDescriptionDto(new GetSkillDto(1, "name", true), "description");

        when(userTokenService.getVerificationByProviderId(Optional.of("abc"))).thenReturn(new VerificationDto(VerificationType.STUDENT, 1, null));
        doNothing().when(studentService).editStudentSkillDescription(1, skillWithDescriptionDto);

        // Act
        ResponseEntity<?> response = sut.editStudentSkillDescription(skillWithDescriptionDto, new UserInfo(Provider.GITHUB, "abc", "name", "url", "email"));

        // Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void test_editStudentSkills_valid() {
        // Arrange
        List<Integer> skills = List.of(1, 2, 3);

        when(userTokenService.getVerificationByProviderId(Optional.of("abc"))).thenReturn(new VerificationDto(VerificationType.STUDENT, 1, null));
        doNothing().when(studentService).updateStudentSkills(1, skills);

        // Act
        ResponseEntity<?> response = sut.updateStudentSkills(skills, new UserInfo(Provider.GITHUB, "abc", "name", "url", "email"));

        // Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void test_getStudentEmails_valid() {
        // Arrange
        when(userTokenService.getVerificationByProviderId(Optional.of("abc"))).thenReturn(new VerificationDto(VerificationType.STUDENT, 1, 1));
        when(taskService.findById(1)).thenReturn(Optional.of(new TaskDto(1, 1, "title", "description", 1)));
        when(projectService.getProject(1)).thenReturn(new GetProjectDto(1, "title", "description", Collections.emptyList(), new BusinessDto(1, "name", "description", new FileDto(Optional.of("file.png")), "location"), new FileDto(Optional.of("img.png"))));

        Collection<String> emails = List.of("email1@email.com", "email2@email.com");
        when(registrationService.getEmailAddressesForRegistrations(1, 1)).thenReturn(emails);

        // Act
        ResponseEntity<Collection<String>> response = sut.getStudentEmails(1, 1, new UserInfo(Provider.GITHUB, "abc", "name", "url", "email"));

        // Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        Assertions.assertTrue(response.hasBody());
        Assertions.assertEquals(emails, response.getBody());
    }

    @Test
    public void test_getStudentEmails_taskNotFound() {
        // Arrange
        when(userTokenService.getVerificationByProviderId(Optional.of("abc"))).thenReturn(new VerificationDto(VerificationType.STUDENT, 1, 1));
        when(taskService.findById(1)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Collection<String>> response = sut.getStudentEmails(1, 1, new UserInfo(Provider.GITHUB, "abc", "name", "url", "email"));

        // Assert
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void test_getStudentEmails_forbidden() {
        // Arrange
        when(userTokenService.getVerificationByProviderId(Optional.of("abc"))).thenReturn(new VerificationDto(VerificationType.STUDENT, 1, 1));
        when(taskService.findById(1)).thenReturn(Optional.of(new TaskDto(1, 1, "title", "description", 1)));
        when(projectService.getProject(1)).thenReturn(new GetProjectDto(1, "title", "description", Collections.emptyList(), new BusinessDto(2, "name", "description", new FileDto(Optional.of("file.png")), "location"), new FileDto(Optional.of("img.png"))));

        // Act
        ResponseEntity<Collection<String>> response = sut.getStudentEmails(1, 1, new UserInfo(Provider.GITHUB, "abc", "name", "url", "email"));

        // Assert
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    // @Test
    // public void test_addSkillToStudent_ValidRequest() throws AuthenticationException {
    //     // Arrange
    //     int userId = 1;
    //     Collection<Integer> newSkillIds = new ArrayList<>();
    //     newSkillIds.add(1);
    //     newSkillIds.add(2);
    //     newSkillIds.add(3);
    //     newSkillIds.add(4);
    //
    //     String authToken = "validToken";
    //
    //     VerificationDTO verification = new VerificationDTO(VerificationType.STUDENT, userId, null);
    //     when(userTokenService.getVerificationByProviderId(Optional.of(authToken))).thenReturn(verification);
    //
    //     // Act
    //     ResponseEntity<?> response = sut.addSkillToStudent(newSkillIds, new UserInfo(Provider.GITHUB, authToken, null, null, null));
    //
    //     // Assert
    //     Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
    //     verify(studentService, times(1)).addSkillToStudent(userId, newSkillIds);
    // }
    //
    //
    // @Test
    // public void test_addSkillToStudent_ServiceThrowsException() throws AuthenticationException {
    //     // Arrange
    //     int userId = 1;
    //     Collection<Integer> newSkillIds = new ArrayList<>();
    //     newSkillIds.add(1);
    //     newSkillIds.add(2);
    //     newSkillIds.add(3);
    //     newSkillIds.add(4);
    //     String authToken = "validToken";
    //
    //     VerificationDTO verification = new VerificationDTO(VerificationType.STUDENT, userId, null);
    //     when(userTokenService.getVerificationByProviderId(Optional.of(authToken))).thenReturn(verification);
    //     doThrow(new RuntimeException("Service Error")).when(studentService).addSkillToStudent(userId, newSkillIds);
    //
    //     // Act & Assert
    //     RuntimeException exception = assertThrows(RuntimeException.class, () ->
    //             sut.addSkillToStudent(newSkillIds, new UserInfo(Provider.GITHUB, authToken, null, null, null))
    //     );
    //     assertEquals("Service Error", exception.getMessage());
    //     verify(studentService, times(1)).addSkillToStudent(userId, newSkillIds);
    // }
}

