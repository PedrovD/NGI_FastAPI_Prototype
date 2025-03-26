package com.han.pwac.pinguins.backend.services;

import com.han.pwac.pinguins.backend.domain.DTO.*;
import com.han.pwac.pinguins.backend.domain.relations.TaskSkillRelation;
import com.han.pwac.pinguins.backend.exceptions.DatabaseInsertException;
import com.han.pwac.pinguins.backend.exceptions.NotFoundException;
import com.han.pwac.pinguins.backend.exceptions.ProjectInvalidBodyException;
import com.han.pwac.pinguins.backend.repository.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {
    @InjectMocks
    private ProjectService sut;
    @Mock
    private ProjectDao projectDAO;
    @Mock
    private BusinessDao businessDAO;
    @Mock
    private TaskDao taskDAO;
    @Mock
    private SupervisorDao supervisorDAO;
    @Mock
    private SkillDao skillDAO;
    @Mock
    private CreateProjectDto project;
    @Mock
    private RegistrationsDao registrationsDao;

    @Test
    public void getProject_successfulAttempt() {
        // Arrange
        int projectId = 1;
        ProjectDto project = new ProjectDto(projectId, "Title", "Description", new FileDto(Optional.of("/path.png")));
        BusinessDto business = new BusinessDto(1, "Business Name", "Description", new FileDto(Optional.of("imagePath")), "location");
        List<GetSkillDto> skills = List.of(new GetSkillDto(1, "Skill Name", false));
        GetProjectDto expected = new GetProjectDto(projectId, "Title", "Description", skills, business, new FileDto(Optional.of("/path.png")));

        when(projectDAO.findById(projectId)).thenReturn(Optional.of(project));
        when(businessDAO.getByProjectId(1)).thenReturn(Optional.of(business));

        // Act
        GetProjectDto response = sut.getProject(projectId);

        // Assert
        assertEquals(response.id(), expected.id());
        assertEquals(response.title(), expected.title());
        assertEquals(response.description(), expected.description());
        assertEquals(response.business(), expected.business());
        assertEquals(response.photo(), expected.photo());
    }

    @Test
    public void getProject_negativeProjectId() {
        // Arrange
        int invalidProjectId = -1;

        // Act + Assert
        assertThrows(
                NotFoundException.class,
                () -> sut.getProject(invalidProjectId)
        );
    }

    @Test
    public void getProject_projectIdZero() {
        // Arrange
        int invalidProjectId = 0;

        // Act + Assert
        assertThrows(
                NotFoundException.class,
                () -> sut.getProject(invalidProjectId)
        );
    }

    @Test
    public void getProject_projectNotFound() {
        // Arrange
        int projectId = 2;
        when(projectDAO.findById(projectId)).thenThrow(NotFoundException.class);

        // Act + Assert
        assertThrows(
                NotFoundException.class,
                () -> sut.getProject(projectId)
        );
    }

    @Test
    public void createProject_successfulAttempt1() {
        // Arrange
        int projectId = 1;
        when(projectDAO.storeProject(any(), anyInt())).thenReturn(projectId);
        when(project.getTitle()).thenReturn(Optional.of("title"));
        when(project.getDescription()).thenReturn(Optional.of("description"));

        // Act
        int response = sut.createProject(project, 1);

        // Assert
        assertEquals(response, projectId);
        verify(projectDAO, times(1)).storeProject(
                eq(project), anyInt()
        );
    }

    @Test
    public void createProject_successfulAttempt2() {
        // Arrange
        int projectId = 5;
        when(projectDAO.storeProject(any(), anyInt())).thenReturn(projectId);
        when(project.getTitle()).thenReturn(Optional.of("Project1"));
        when(project.getDescription()).thenReturn(Optional.of("Description1"));

        // Act
        int response = sut.createProject(project, 1);

        // Assert
        assertEquals(response, projectId);
        verify(projectDAO, times(1)).storeProject(
                eq(project), anyInt()
        );
    }

    @Test
    public void createProject_DAOThrowsInsertException() {
        // Arrange
        when(projectDAO.storeProject(any(), anyInt())).thenThrow(DatabaseInsertException.class);
        when(project.getTitle()).thenReturn(Optional.of("Project1"));
        when(project.getDescription()).thenReturn(Optional.of("Description1"));

        // Act + Assert
        assertThrows(
                DatabaseInsertException.class,
                () -> sut.createProject(project, 1)
        );
    }

    @Test
    public void createProject_withTitleEmpty() {
        // Arrange
        when(project.getTitle()).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                ProjectInvalidBodyException.class,
                () -> sut.createProject(project, 1)
        );
    }

    @Test
    public void createProject_withTitleNull() {
        // Arrange
        when(project.getTitle()).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                ProjectInvalidBodyException.class,
                () -> sut.createProject(project, 1)
        );
    }

    @Test
    public void createProject_withTitleTooLong() {
        // Arrange
        when(project.getTitle()).thenReturn(Optional.of("a".repeat(51))); // max 50 chars

        // Act + Assert
        assertThrows(
                ProjectInvalidBodyException.class,
                () -> sut.createProject(project, 1)
        );
    }

    @Test
    public void createProject_withTitleExactly50Chars() {
        // Arrange
        when(project.getTitle()).thenReturn(Optional.of("a".repeat(50))); // max 50 chars
        when(project.getDescription()).thenReturn(Optional.of("Description1"));

        // Act + Assert
        sut.createProject(project, 1);
    }

    @Test
    public void createProject_withDescriptionEmpty() {
        // Arrange
        when(project.getTitle()).thenReturn(Optional.of("Project1"));
        when(project.getDescription()).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                ProjectInvalidBodyException.class,
                () -> sut.createProject(project, 1)
        );
    }

    @Test
    public void createProject_withDescriptionNull() {
        // Arrange
        when(project.getTitle()).thenReturn(Optional.of("Project1"));
        when(project.getDescription()).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                ProjectInvalidBodyException.class,
                () -> sut.createProject(project, 1)
        );
    }

    @Test
    public void createProject_withDescriptionTooLong() {
        // Arrange
        when(project.getTitle()).thenReturn(Optional.of("Project1"));
        when(project.getDescription()).thenReturn(Optional.of("a".repeat(4001))); // max 4000 chars

        // Act + Assert
        assertThrows(
                ProjectInvalidBodyException.class,
                () -> sut.createProject(project, 1)
        );
    }

    @Test
    public void createProject_withDescriptionExactly4000Chars() {
        // Arrange
        when(project.getTitle()).thenReturn(Optional.of("Project1"));
        when(project.getDescription()).thenReturn(Optional.of("a".repeat(4000))); // max 4000 chars

        // Act + Assert
        sut.createProject(project, 1);
    }

    @Test
    public void createProject_withDuplicateName() {
        // Arrange
        when(project.getTitle()).thenReturn(Optional.of("Project1"));
        when(project.getDescription()).thenReturn(Optional.of("Description1"));
        when(projectDAO.checkIfProjectNameIsTaken("Project1", 1)).thenReturn(Optional.of(true));

        // Act + Assert
        assertThrows(
                ProjectInvalidBodyException.class,
                () -> sut.createProject(project, 1)
        );
    }

    @Test
    public void test_ProjectServiceGetAll_VerifyReturnValues() {
        // arrange
        List<ProjectDto> projectDtos = new ArrayList<>();
        ProjectDto firstProject = new ProjectDto(1, "title", "desc", new FileDto(Optional.of("photo")));
        projectDtos.add(firstProject);
        when(projectDAO.getAll()).thenReturn(projectDtos);

        BusinessDto businessDto = new BusinessDto(1, "name", "businessDesc", new FileDto(Optional.of("photo")), "location");
        when(businessDAO.getByProjectId(1)).thenReturn(Optional.of(businessDto));

        List<GetSkillDto> skillDtos = new ArrayList<>();
        skillDtos.add(new GetSkillDto(1, "test", false));

        // act
        Collection<GetProjectDto> projects = sut.getAllProjectsWithSkills();
        GetProjectDto project = projects.stream().findFirst().get();

        // assert
        Assertions.assertEquals(project.id(), firstProject.id());
        Assertions.assertEquals(project.title(), firstProject.title());
        Assertions.assertEquals(project.photo(), firstProject.photo());
        Assertions.assertEquals(project.description(), firstProject.description());
        Assertions.assertEquals(project.business(), businessDto);
    }

    @Test
    public void getAllProjectsWithSkills_successfulAttempt() {
        // Arrange
        ArrayList<ProjectDto> projects = new ArrayList<>();
        projects.add(new ProjectDto(1, "title", "desc", new FileDto(Optional.of("photo"))));
        when(projectDAO.getAll()).thenReturn(projects);
        when(businessDAO.getByProjectId(1)).thenReturn(Optional.of(new BusinessDto(1, "name", "businessDesc", new FileDto(Optional.of("photo")), "location")));

        // Act
        Collection<GetProjectDto> response = sut.getAllProjectsWithSkills();

        // Assert
        assertEquals(response.size(), 1);
    }

    @Test
    public void getAllProjectsWithSkills_emptyList() {
        // Arrange
        when(projectDAO.getAll()).thenReturn(new ArrayList<>());

        // Act
        Collection<GetProjectDto> response = sut.getAllProjectsWithSkills();

        // Assert
        assertEquals(response.size(), 0);
    }

    @Test
    public void getAllByBusinessId_successfulAttempt() {
        // Arrange
        int businessId = 1;
        ArrayList<ProjectDto> projects = new ArrayList<>();
        projects.add(new ProjectDto(1, "title", "desc", new FileDto(Optional.of("photo"))));
        when(projectDAO.getAllByBusinessId(businessId)).thenReturn(projects);
        when(businessDAO.getByProjectId(1)).thenReturn(Optional.of(new BusinessDto(1, "name", "businessDesc", new FileDto(Optional.of("photo")), "location")));

        // Act
        Collection<GetProjectDto> response = sut.getAllByBusinessId(businessId);

        // Assert
        assertEquals(response.size(), 1);
    }

    @Test
    public void getAllByBusinessId_emptyList() {
        // Arrange
        int businessId = 1;
        when(projectDAO.getAllByBusinessId(businessId)).thenReturn(new ArrayList<>());

        // Act
        Collection<GetProjectDto> response = sut.getAllByBusinessId(businessId);

        // Assert
        assertEquals(response.size(), 0);
    }

    @Test
    public void getProjectWithDetails_successfulAttempt() {
        // Arrange
        int projectId = 1;
        ArrayList<TaskDto> tasks = new ArrayList<>();
        tasks.add(new TaskDto(1, 1, "title", "desc", 1));
        when(taskDAO.getByProjectId(projectId)).thenReturn(tasks);
        lenient().when(taskDAO.findById(1)).thenReturn(Optional.of(new TaskDto(1, 1, "title", "desc", 1)));
        when(projectDAO.findById(projectId)).thenReturn(Optional.of(new ProjectDto(projectId, "title", "desc", new FileDto(Optional.of("photo")))));

        // Act
        ProjectWithTasksAndSkillsDto response = sut.getProjectWithDetails(projectId);

        // Assert
        assertEquals(response.getProjectId(), projectId);
        assertEquals(response.getTasks().getFirst().getTaskId(), 1);
        assertEquals(response.getProjectId(), 1);
        assertEquals(response.getTitle(), "title");
        assertEquals(response.getDescription(), "desc");
        assertEquals(response.getImage().path().get(), "photo");
    }

    @Test
    public void getProjectWithDetails_withProjectNull() {
        // Arrange
        int projectId = 1;
        when(projectDAO.findById(projectId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                NotFoundException.class,
                () -> sut.getProjectWithDetails(projectId)
        );
    }

    @Test
    public void getAllBusinessesWithProjectsAndTasks_emptyList() {
        // Arrange
        when(businessDAO.getAll()).thenReturn(new ArrayList<>());

        // Act
        Collection<BusinessProjectsWithTasksAndSkillsDto> response = sut.getAllBusinessesWithProjectsAndTasks();

        // Assert
        assertEquals(response.size(), 0);
    }
}
