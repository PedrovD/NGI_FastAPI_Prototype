package com.han.pwac.pinguins.backend;

import com.han.pwac.pinguins.backend.domain.DTO.*;
import com.han.pwac.pinguins.backend.domain.relations.TaskSkillRelation;
import com.han.pwac.pinguins.backend.exceptions.NotFoundException;
import com.han.pwac.pinguins.backend.repository.*;
import com.han.pwac.pinguins.backend.repository.contract.IBusinessDao;
import com.han.pwac.pinguins.backend.repository.contract.IProjectDao;
import com.han.pwac.pinguins.backend.repository.contract.ISkillDao;
import com.han.pwac.pinguins.backend.services.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private SupervisorDao supervisorDAO;

    @Mock
    private IProjectDao projectDAO;

    @Mock
    private TaskDao taskDAO;

    @Mock
    private ISkillDao skillDAO;

    @Mock
    private IBusinessDao businessDAO;

    @Mock
    private RegistrationsDao registrationsDao;

    @InjectMocks
    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetTaskWithSkills_HappyPath() {
        // Arrange
        ProjectService service = new ProjectService(projectDAO, businessDAO, skillDAO, supervisorDAO, taskDAO, registrationsDao);

        int taskId = 1;
        TaskDto task = new TaskDto(
                taskId,
                1,
                "Sample Task",
                "Description",
                5
        );

        TaskSkillRelation relation = new TaskSkillRelation();
        relation.setSkillId(1);

        GetSkillDto skill = new GetSkillDto(1, "Java", false);

        when(taskDAO.findById(taskId)).thenReturn(Optional.of(task));
        when(skillDAO.getAllForTask(1)).thenReturn(List.of(skill));


        // Act
        TaskWithSkills taskWithSkills = service.getTaskWithSkills(taskId);

        // Assert
        assertNotNull(taskWithSkills);
        assertEquals(task.getTaskId(), taskWithSkills.getTaskId());
        assertEquals(task.getTitle(), taskWithSkills.getTitle());
        assertEquals(1, taskWithSkills.getSkills().size());
        assertEquals("Java", taskWithSkills.getSkills().stream().findFirst().get().name());
    }

    @Test
    void testGetTaskWithSkills_TaskNotFoundException() {
        // Arrange
        ProjectService service = new ProjectService(projectDAO, businessDAO, skillDAO, supervisorDAO, taskDAO, registrationsDao);

        int taskId = 1;
        when(taskDAO.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> service.getTaskWithSkills(taskId));
    }


    @Test
    void testGetProjectWithDetails_ProjectNotFoundException() {
        // Arrange
        ProjectService service = new ProjectService(projectDAO, businessDAO, skillDAO, supervisorDAO, taskDAO, registrationsDao);

        int projectId = 1;
        when(projectDAO.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> service.getProjectWithDetails(projectId));
    }
}