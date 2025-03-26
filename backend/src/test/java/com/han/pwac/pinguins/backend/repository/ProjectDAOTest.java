package com.han.pwac.pinguins.backend.repository;

import com.han.pwac.pinguins.backend.domain.DTO.CreateProjectDto;
import com.han.pwac.pinguins.backend.domain.DTO.FileDto;
import com.han.pwac.pinguins.backend.domain.DTO.ProjectDto;
import com.han.pwac.pinguins.backend.exceptions.DatabaseInsertException;
import com.han.pwac.pinguins.backend.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectDAOTest {
    @InjectMocks
    private ProjectDao sut;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private CreateProjectDto project;

    @Test
    public void storeProject_successfulAttempt1() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(12);
        lenient().when(jdbcTemplate.update(anyString(), anyInt(), anyString(), anyString(), anyString())).thenReturn(1);
        when(project.getTitle()).thenReturn(Optional.of("Title"));
        when(project.getDescription()).thenReturn(Optional.of("Description"));
        when(project.getImagePath()).thenReturn(new FileDto(Optional.of("abc.jpg")));

        // Act
        int response = sut.storeProject(project, 1);

        // Assert
        assertEquals(response, 12);
        verify(jdbcTemplate, times(1)).update(
                anyString(), eq(1), eq("Title"), eq("Description"), anyString()
        );
    }

    @Test
    public void storeProject_successfulAttempt2() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(250);
        when(jdbcTemplate.update(anyString(), anyInt(), anyString(), anyString(), anyString())).thenReturn(1);
        when(project.getTitle()).thenReturn(Optional.of("Andere titel"));
        when(project.getDescription()).thenReturn(Optional.of("Andere description"));
        when(project.getImagePath()).thenReturn(new FileDto(Optional.of("abc.jpg")));

        // Act
        int response = sut.storeProject(project, 2);

        // Assert
        assertEquals(response, 250);
        verify(jdbcTemplate, times(1)).update(
                anyString(), eq(2), eq("Andere titel"), eq("Andere description"), anyString()
        );
    }

    @Test
    public void storeProject_dataNotInserted() {
        // Arrange
        when(jdbcTemplate.update(anyString(), anyInt(), anyString(), anyString(), anyString())).thenReturn(0);
        when(project.getTitle()).thenReturn(Optional.of("Title"));
        when(project.getDescription()).thenReturn(Optional.of("Description"));
        when(project.getImagePath()).thenReturn(new FileDto(Optional.of("abc.jpg")));

        // Act + Assert
        assertThrows(
                DatabaseInsertException.class,
                () -> sut.storeProject(project, 3)
        );
    }

    @Test
    public void selectLastInsertedId_successfulAttempt() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(16);

        // Act
        Integer response = sut.getLastInsertedId();

        // Assert
        assertEquals(response, 16);
        verify(jdbcTemplate, times(1)).queryForObject(anyString(), eq(Integer.class));
    }

    @Test
    public void checkIfProjectExists_projectExists() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(1))).thenReturn(1);

        // Act
        Optional<Boolean> response = sut.checkIfProjectExists(1);

        // Assert
        assertTrue(response.isPresent());
        assertEquals(response.get(), true);
        verify(jdbcTemplate, times(1)).queryForObject(anyString(), eq(Integer.class), eq(1));
    }

    @Test
    public void checkIfProjectExists_projectDoesNotExist() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(2))).thenReturn(0);

        // Act
        Optional<Boolean> response = sut.checkIfProjectExists(2);

        // Assert
        assertTrue(response.isPresent());
        assertEquals(response.get(), false);
        verify(jdbcTemplate, times(1)).queryForObject(anyString(), eq(Integer.class), eq(2));
    }

    @Test
    public void getProjectById_successfulAttempt() {
        // Arrange
        ProjectDto project = new ProjectDto(1, "Title", "Description", new FileDto(Optional.of("/path.png")));
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(1))).thenReturn(project);

        // Act
        Optional<ProjectDto> response = sut.findById(1);

        // Assert
        assertTrue(response.isPresent());
        assertEquals(response.get(), project);
    }

    @Test
    public void getProjectById_projectDoesNotExist() {
        // Arrange
        lenient().when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(2))).thenThrow(EmptyResultDataAccessException.class);

        // Act + Assert
        assertThrows(
                NotFoundException.class,
                () -> sut.findById(2)
        );
    }

    @Test
    public void checkIfProjectNameIsTaken_nameIsNotTaken() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString(), anyInt())).thenReturn(0);

        // Act
        Optional<Boolean> response = sut.checkIfProjectNameIsTaken("ProjectName", 1);

        // Assert
        assertTrue(response.isPresent());
        assertEquals(response.get(), false);
        verify(jdbcTemplate, times(1)).queryForObject(anyString(), eq(Integer.class), eq("ProjectName"), eq(1));
    }

    @Test
    public void checkIfProjectNameIsTaken_nameIsTaken() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString(), anyInt())).thenReturn(1);

        // Act
        Optional<Boolean> response = sut.checkIfProjectNameIsTaken("ProjectName", 1);

        // Assert
        assertTrue(response.isPresent());
        assertEquals(response.get(), true);
        verify(jdbcTemplate, times(1)).queryForObject(anyString(), eq(Integer.class), eq("ProjectName"), eq(1));
    }
}
