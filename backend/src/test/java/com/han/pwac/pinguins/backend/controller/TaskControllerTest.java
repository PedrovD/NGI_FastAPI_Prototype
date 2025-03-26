package com.han.pwac.pinguins.backend.controller;

import com.han.pwac.pinguins.backend.authentication.Provider;
import com.han.pwac.pinguins.backend.domain.DTO.TaskDto;
import com.han.pwac.pinguins.backend.domain.DTO.TaskWithSkills;
import com.han.pwac.pinguins.backend.domain.UserInfo;
import com.han.pwac.pinguins.backend.services.TaskService;
import com.han.pwac.pinguins.backend.services.UserTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.naming.AuthenticationException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskControllerTest {
    @InjectMocks
    private TaskController sut;
    @Mock
    private TaskService taskService;
    @Mock
    private UserTokenService userTokenService;

    @Test
    public void getTasks_successfulAttempt() {
        // Arrange
        int projectId = 1;
        List<TaskWithSkills> tasks = new ArrayList<>();
        when(taskService.getTasksByProjectId(projectId)).thenReturn(tasks);

        // Act
        List<TaskWithSkills> response = sut.getTasks(projectId);

        // Assert
        assertEquals(response, tasks);
    }

    @Test
    void createTask_successfulAttempt() throws AuthenticationException {
        // Arrange
        int projectId = 1;
        String authToken = "validAuthToken";
        TaskDto task = new TaskDto(1, projectId, "Title", "Description", 5);

        // Act
        when(userTokenService.checkIfProviderIdMatchesBusinessId(authToken, projectId)).thenReturn(true);
        ResponseEntity<?> response = sut.createTask(projectId, task, new UserInfo(Provider.GITHUB, authToken, null, null, null));

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Taak toegevoegd.", response.getBody());
        verify(taskService).addTaskToProject(projectId, task);
    }
    @Test
    void createTask_unauthorizedAttempt() {
        // Arrange
        int projectId = 1;
        String authToken = "invalidAuthToken";
        TaskDto task = new TaskDto(1, projectId, "Title", "Description", 5);

        // Act & Assert
        when(userTokenService.checkIfProviderIdMatchesBusinessId(authToken, projectId)).thenReturn(false);
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> sut.createTask(projectId, task, new UserInfo(Provider.GITHUB, authToken, null, null, null))
        );

        assertEquals("U bent niet gemachtigd", exception.getMessage());
        verify(taskService, never()).addTaskToProject(anyInt(), any(TaskDto.class));
    }
}
