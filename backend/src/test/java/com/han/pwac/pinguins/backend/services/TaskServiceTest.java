package com.han.pwac.pinguins.backend.services;

import com.han.pwac.pinguins.backend.domain.DTO.*;
import com.han.pwac.pinguins.backend.domain.EmailPart;
import com.han.pwac.pinguins.backend.exceptions.NotFoundException;
import com.han.pwac.pinguins.backend.exceptions.TaskInvalidBodyException;
import com.han.pwac.pinguins.backend.repository.ProjectDao;
import com.han.pwac.pinguins.backend.repository.RegistrationsDao;
import com.han.pwac.pinguins.backend.repository.SkillDao;
import com.han.pwac.pinguins.backend.repository.TaskDao;
import com.han.pwac.pinguins.backend.repository.contract.ICronJobDao;
import com.han.pwac.pinguins.backend.services.contract.IBaseService;
import org.hibernate.annotations.NotFound;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {
    @InjectMocks
    private TaskService sut;
    @Mock
    private TaskDao taskDAO;
    @Mock
    private ProjectDao projectDAO;
    @Mock
    private SkillDao skillDAO;
    @Mock
    private RegistrationsDao registrationsDao;
    @Mock
    private IBaseService<StudentRepositoryDto> studentService;
    @Mock
    private ICronJobDao cronJobDao;

    @Test
    public void addTaskToProject_happy() {
        // Arrange
        int projectId = 1;
        TaskDto task = new TaskDto(1, projectId, "Title", "Description", 3);

        when(projectDAO.checkIfProjectExists(projectId)).thenReturn(Optional.of(true));
        when(taskDAO.findById(task.getTaskId())).thenReturn(Optional.empty());
        when(taskDAO.getByProjectId(projectId)).thenReturn(List.of(new TaskDto(2, projectId, "Andere Title", "Description", 3)));

        // Act
        sut.addTaskToProject(projectId, task);

        // Assert
        verify(taskDAO, times(1)).addTask(task);
    }

    @Test
    public void addTaskToProject_projectIdIsZero() {
        // Arrange
        int projectId = 0;
        TaskDto task = new TaskDto(1, projectId, "Title", "Description", 3);
        when(projectDAO.checkIfProjectExists(projectId)).thenReturn(Optional.of(false));

        // Act + Assert
        assertThrows(
                NotFoundException.class,
                () -> sut.addTaskToProject(projectId, task)
        );
    }

    @Test
    public void addTaskToProject_titleOver50Characters() {
        // Arrange
        int projectId = 1;
        TaskDto task = new TaskDto(1, projectId, "a".repeat(51), "Description", 3);
        when(projectDAO.checkIfProjectExists(projectId)).thenReturn(Optional.of(true));

        // Act + Assert
        assertThrows(
                TaskInvalidBodyException.class,
                () -> sut.addTaskToProject(projectId, task)
        );
    }

    @Test
    public void addTaskToProject_titleExactly50Characters() {
        // Arrange
        int projectId = 1;
        TaskDto task = new TaskDto(1, projectId, "a".repeat(50), "Description", 3);
        when(projectDAO.checkIfProjectExists(projectId)).thenReturn(Optional.of(true));
        when(taskDAO.findById(task.getTaskId())).thenReturn(Optional.empty());

        // Act
        sut.addTaskToProject(projectId, task);

        // Assert
        verify(taskDAO, times(1)).addTask(task);
    }

    @Test
    public void addTaskToProject_DescriptionLongerThan4000Characters() {
        // Arrange
        int projectId = 1;
        TaskDto task = new TaskDto(1, projectId, "Title", "a".repeat(4001), 3);
        when(projectDAO.checkIfProjectExists(projectId)).thenReturn(Optional.of(true));

        // Act + Assert
        assertThrows(
                TaskInvalidBodyException.class,
                () -> sut.addTaskToProject(projectId, task)
        );
    }

    @Test
    public void addTaskToProject_DescriptionExactly4000Characters() {
        // Arrange
        int projectId = 1;
        TaskDto task = new TaskDto(1, projectId, "Title", "a".repeat(4000), 3);
        when(projectDAO.checkIfProjectExists(projectId)).thenReturn(Optional.of(true));
        when(taskDAO.findById(task.getTaskId())).thenReturn(Optional.empty());

        // Act
        sut.addTaskToProject(projectId, task);

        // Assert
        verify(taskDAO, times(1)).addTask(task);
    }

    @Test
    public void addTaskToProject_taskAlreadyExists() {
        // Arrange
        int projectId = 1;
        TaskDto task = new TaskDto(1, projectId, "Title", "Description", 3);

        when(projectDAO.checkIfProjectExists(projectId)).thenReturn(Optional.of(true));
        when(taskDAO.findById(task.getTaskId())).thenReturn(Optional.of(task));

        // Act + Assert
        assertThrows(
                TaskInvalidBodyException.class,
                () -> sut.addTaskToProject(projectId, task)
        );
    }

    @Test
    public void addTaskToProject_TaskTitleAlreadyExists() {
        // Arrange
        int projectId = 1;
        TaskDto task = new TaskDto(1, projectId, "Title", "Description", 3);
        TaskDto task2 = new TaskDto(2, projectId, "Title", "Description", 3);

        when(projectDAO.checkIfProjectExists(projectId)).thenReturn(Optional.of(true));
        when(taskDAO.findById(task.getTaskId())).thenReturn(Optional.empty());
        when(taskDAO.getByProjectId(projectId)).thenReturn(List.of(task2));

        // Act + Assert
        assertThrows(
                TaskInvalidBodyException.class,
                () -> sut.addTaskToProject(projectId, task)
        );
    }

    @Test
    public void getTasksByProjectId_successfulAttempt() {
        // Arrange
        int projectId = 1;
        List<TaskDto> tasks = List.of(
                new TaskDto(1, projectId, "Title", "Description", 3),
                new TaskDto(2, projectId, "Title", "Description", 3)
        );
        List<TaskWithSkills> expected = new ArrayList<>();
        for (TaskDto task : tasks) {
            expected.add(new TaskWithSkills(task, 0, 0, new ArrayList<>()));
        }
        when(registrationsDao.getAll()).thenReturn(List.of(
                new RegistrationDto(1, 1, 1, "hallo", Optional.of(true), "reactie"),
                new RegistrationDto(2, 1, 1, "hallo", Optional.of(false), "reactie"),
                new RegistrationDto(2, 1, 1, "hallo", Optional.empty(), "reactie"),
                new RegistrationDto(2, 1, 1, "hallo", Optional.empty(), "reactie"),
                new RegistrationDto(3, 2, 2, "hallo", Optional.of(true), "reactie"),
                new RegistrationDto(4, 2, 2, "hallo", Optional.of(false), "reactie"),
                new RegistrationDto(4, 2, 2, "hallo", Optional.of(true), "reactie"),
                new RegistrationDto(4, 2, 2, "hallo", Optional.of(true), "reactie"),
                new RegistrationDto(4, 2, 2, "hallo", Optional.empty(), "reactie")
        ));

        when(projectDAO.checkIfProjectExists(projectId)).thenReturn(Optional.of(true));
        when(taskDAO.getTasksByProjectId(projectId)).thenReturn(tasks);
        when(skillDAO.getTaskSkills(anyInt())).thenReturn(List.of());

        // Act
        List<TaskWithSkills> response = sut.getTasksByProjectId(projectId);

        // Assert
        assertEquals(response.getFirst().getTaskId(), expected.getFirst().getTaskId());
        assertEquals(response.getFirst().getTitle(), expected.getFirst().getTitle());
        assertEquals(response.getFirst().getDescription(), expected.getFirst().getDescription());
        assertEquals(response.getFirst().getTotalNeeded(), expected.getFirst().getTotalNeeded());
        assertEquals(response.getFirst().getSkills(), expected.getFirst().getSkills());
        assertEquals(response.getFirst().getTotalAccepted(), 1);
        assertEquals(response.getFirst().getTotalRegistered(), 2);

        assertEquals(response.getLast().getTaskId(), expected.getLast().getTaskId());
        assertEquals(response.getLast().getTitle(), expected.getLast().getTitle());
        assertEquals(response.getLast().getDescription(), expected.getLast().getDescription());
        assertEquals(response.getLast().getTotalNeeded(), expected.getLast().getTotalNeeded());
        assertEquals(response.getLast().getSkills(), expected.getLast().getSkills());
        assertEquals(response.getLast().getTotalAccepted(), 3);
        assertEquals(response.getLast().getTotalRegistered(), 1);
    }

    @Test
    public void getTasksByProjectId_negativeProjectId() {
        int invalidProjectId = -1;

        assertThrows(
                NotFoundException.class,
                () -> sut.getTasksByProjectId(invalidProjectId)
        );
    }

    @Test
    public void getTasksByProjectId_zeroProjectId() {
        int invalidProjectId = 0;

        assertThrows(
                NotFoundException.class,
                () -> sut.getTasksByProjectId(invalidProjectId)
        );
    }

    @Test
    public void getTasksByProjectId_projectNotFound() {
        int projectId = 2;

        when(projectDAO.checkIfProjectExists(projectId)).thenReturn(Optional.of(false));

        assertThrows(
                NotFoundException.class,
                () -> sut.getTasksByProjectId(projectId)
        );
    }

    @Test
    public void getTasksByProjectId_noTasksFound() {
        int projectId = 1;
        List<TaskDto> tasks = new ArrayList<>();

        when(projectDAO.checkIfProjectExists(projectId)).thenReturn(Optional.of(true));
        when(taskDAO.getTasksByProjectId(projectId)).thenReturn(tasks);

        List<TaskWithSkills> response = sut.getTasksByProjectId(projectId);

        assertTrue(response.isEmpty());
    }

    @Test
    public void getProjectIdFromTasks_happy() {
        // Arrange
        int taskId = 1;
        int projectId = 1;
        when(taskDAO.GetProjectIdFromTask(taskId)).thenReturn(Optional.of(projectId));

        // Act
        int response = sut.getProjectIdFromTasks(taskId);

        // Assert
        assertEquals(projectId, response);
    }

    @Test
    public void getProjectIdFromTasks_taskNotFound() {
        // Arrange
        int taskId = 1;
        when(taskDAO.GetProjectIdFromTask(taskId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                NotFoundException.class,
                () -> sut.getProjectIdFromTasks(taskId)
        );
    }

    @Test
    public void findById_happy() {
        // Arrange
        int taskId = 1;
        TaskDto task = new TaskDto(1, 1, "Title", "Description", 3);
        when(taskDAO.findById(taskId)).thenReturn(Optional.of(task));

        // Act
        TaskDto response = sut.findById(taskId).get();

        // Assert
        assertEquals(task, response);
        verify(taskDAO, times(1)).findById(taskId);
    }

    @Test
    public void test_getBatchedMails_valid() {
        // Arrange
        Timestamp d = new Timestamp(142192930);
        ArgumentCaptor<Timestamp> timestampArgumentCaptor = ArgumentCaptor.forClass(Timestamp.class);

        when(cronJobDao.getPreviousRunDate()).thenReturn(d);
        when(taskDAO.getAllTasksAfter(timestampArgumentCaptor.capture())).thenReturn(List.of(new TaskDto(1, 3, "title", "description", 1)));

        when(studentService.getAll()).thenReturn(List.of(new StudentRepositoryDto(1, "id", "name", "description", new FileDto(Optional.empty()), new FileDto(Optional.empty()), "email"), new StudentRepositoryDto(2, "id", "name2", "description", new FileDto(Optional.empty()), new FileDto(Optional.empty()), "email"), new StudentRepositoryDto(3, "id", "name3", "description", new FileDto(Optional.empty()), new FileDto(Optional.empty()), "email")));

        when(skillDAO.getTaskSkills(1)).thenReturn(List.of(new GetSkillDto(1, "name", true)));

        when(skillDAO.getAllForStudent(1)).thenReturn(List.of(new GetSkillWithDescriptionDto(new GetSkillDto(1, "name", true), "description")));
        when(skillDAO.getAllForStudent(2)).thenReturn(List.of(new GetSkillWithDescriptionDto(new GetSkillDto(2, "name2", true), "description")));
        when(skillDAO.getAllForStudent(3)).thenReturn(List.of(new GetSkillWithDescriptionDto(new GetSkillDto(1, "name", true), "description")));

        // Act
        Map<Integer, EmailPart> parts = sut.getBatchedMails();

        // Assert

        assertEquals(2, parts.size());

        EmailPart part = parts.get(1);
        assertNotNull(part);
        assertTrue(part.toString().contains("Nieuwe taken die voor jou toepasselijk zijn:"));
        assertTrue(part.toString().contains("title"));

        EmailPart part2 = parts.get(2);
        assertNull(part2);

        EmailPart part3 = parts.get(3);
        assertNotNull(part3);
        assertTrue(part3.toString().contains("Nieuwe taken die voor jou toepasselijk zijn:"));
        assertTrue(part3.toString().contains("title"));

        assertEquals(d, timestampArgumentCaptor.getValue());
    }

    @Test
    public void test_getBatchedMails_empty() {
        // Arrange
        Timestamp d = new Timestamp(142192930);
        ArgumentCaptor<Timestamp> timestampArgumentCaptor = ArgumentCaptor.forClass(Timestamp.class);

        when(cronJobDao.getPreviousRunDate()).thenReturn(d);
        when(taskDAO.getAllTasksAfter(timestampArgumentCaptor.capture())).thenReturn(Collections.emptyList());

        // Act
        Map<Integer, EmailPart> parts = sut.getBatchedMails();

        // Assert

        assertEquals(0, parts.size());

        assertEquals(d, timestampArgumentCaptor.getValue());
    }
}
