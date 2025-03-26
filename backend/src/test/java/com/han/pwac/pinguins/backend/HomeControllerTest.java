package com.han.pwac.pinguins.backend;

import com.han.pwac.pinguins.backend.controller.ProjectController;
import com.han.pwac.pinguins.backend.domain.DTO.BusinessProjectsWithTasksAndSkillsDto;
import com.han.pwac.pinguins.backend.domain.DTO.ProjectWithTasksAndSkillsDto;
import com.han.pwac.pinguins.backend.exceptions.InvalidDataException;
import com.han.pwac.pinguins.backend.services.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class HomeControllerTest {

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController projectController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllBusinessesWithProjectsAndTasks_HappyPath() {
        // Arrange
        List<BusinessProjectsWithTasksAndSkillsDto> mockDTOs = Arrays.asList(new BusinessProjectsWithTasksAndSkillsDto());
        when(projectService.getAllBusinessesWithProjectsAndTasks()).thenReturn(mockDTOs);

        // Act
        ResponseEntity<Collection<BusinessProjectsWithTasksAndSkillsDto>> response = projectController.getAllBusinessesWithProjectsAndTasks();

        // Assert
        verify(projectService, times(1)).getAllBusinessesWithProjectsAndTasks();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockDTOs, response.getBody());
    }

    @Test
    void testGetAllBusinessesWithProjectsAndTasks_InvalidDataException() {
        // Arrange
        when(projectService.getAllBusinessesWithProjectsAndTasks()).thenReturn(List.of());

        // Act & Assert
        try {
            projectController.getAllBusinessesWithProjectsAndTasks();
        } catch (InvalidDataException e) {
            assertEquals("The BusinessProjectsWithTasksAndSkills data could not be retrieved", e.getMessage());
        }

        verify(projectService, times(1)).getAllBusinessesWithProjectsAndTasks();
    }


}