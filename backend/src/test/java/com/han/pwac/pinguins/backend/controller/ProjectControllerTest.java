package com.han.pwac.pinguins.backend.controller;

import com.han.pwac.pinguins.backend.authentication.Provider;
import com.han.pwac.pinguins.backend.domain.DTO.*;
import com.han.pwac.pinguins.backend.domain.UserInfo;
import com.han.pwac.pinguins.backend.domain.VerificationType;
import com.han.pwac.pinguins.backend.exceptions.ProjectInvalidBodyException;
import com.han.pwac.pinguins.backend.services.FileService;
import com.han.pwac.pinguins.backend.services.ProjectService;
import com.han.pwac.pinguins.backend.services.UserTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectControllerTest {
    @InjectMocks
    private ProjectController sut;
    @Mock
    private ProjectService projectService;
    @Mock
    private FileService fileService;
    @Mock
    private CreateProjectDto project;
    @Mock
    private UserTokenService tokenService;
    @Mock
    MultipartFile image;

    @Test
    public void getProject_successfulAttempt() {
        // Arrange
        int projectId = 1;
        GetProjectDto projectForView = new GetProjectDto(1, "title", "description", Collections.emptyList(), new BusinessDto(1, "name", "description", new FileDto(Optional.of("file.png")), "location"), new FileDto(Optional.of("/path.png")));
        when(projectService.getProject(projectId)).thenReturn(projectForView);

        // Act
        ResponseEntity<GetProjectDto> response = sut.getProject(projectId);

        // Assert
        assertEquals(response.getBody(), projectForView);
    }

    @Test
    public void createProject_successfulAttempt1() {
        // Arrange
        int projectId = 1;
        when(projectService.createProject(any(), anyInt())).thenReturn(projectId);
        when(tokenService.getVerificationByProviderId(Optional.of("sessionId"))).thenReturn(new VerificationDto(VerificationType.SUPERVISOR, 1, 1));

        // Act
        int response = sut.createProject("appel", "beschrijving", image, new UserInfo(Provider.GITHUB, "sessionId", null, null, null));

        // Assert
        assertEquals(response, projectId);
        verify(projectService, times(1)).createProject(
                any(), anyInt()
        );
        verify(fileService, times(1)).uploadFile(image, new MimeType("image", "*"));
    }

    @Test
    public void createProject_successfulAttempt2() {
        // Arrange
        int projectId = 5;
        when(projectService.createProject(any(), anyInt())).thenReturn(projectId);
        when(tokenService.getVerificationByProviderId(Optional.of("sessionId"))).thenReturn(new VerificationDto(VerificationType.SUPERVISOR, 1, 1));

        // Act
        int response = sut.createProject("appel", "beschrijving", image, new UserInfo(Provider.GITHUB, "sessionId", null, null, null));

        // Assert
        assertEquals(response, projectId);
        verify(projectService, times(1)).createProject(
                any(), anyInt()
        );
        verify(fileService, times(1)).uploadFile(image, new MimeType("image", "*"));
    }

    @Test
    public void createProject_serviceThrowsProjectInvalidBodyException() {
        // Arrange
        when(projectService.createProject(any(), anyInt())).thenThrow(ProjectInvalidBodyException.class);
        when(tokenService.getVerificationByProviderId(Optional.of("sessionId"))).thenReturn(new VerificationDto(VerificationType.SUPERVISOR, 1, 1));

        // Act + Assert
        assertThrows(
                ProjectInvalidBodyException.class,
                () -> sut.createProject("appel", "beschrijving", image, new UserInfo(Provider.GITHUB, "sessionId", null, null, null))
        );
    }
}
