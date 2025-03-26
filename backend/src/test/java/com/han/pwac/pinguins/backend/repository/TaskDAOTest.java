package com.han.pwac.pinguins.backend.repository;

import com.han.pwac.pinguins.backend.domain.DTO.TaskDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskDAOTest {
    @InjectMocks
    private TaskDao sut;
    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    public void getTasksByProjectId_successfulAttempt() {
        int projectId = 1;
        List<TaskDto> tasks = List.of(new TaskDto(1, projectId, "Title", "Description", 5));
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(projectId))).thenReturn(tasks);

        List<TaskDto> response = sut.getTasksByProjectId(projectId);

        assertEquals(response, tasks);
        verify(jdbcTemplate, times(1)).query(anyString(), any(RowMapper.class), eq(projectId));
    }

    @Test
    public void getTasksByProjectId_noTasksFound() {
        int projectId = 1;
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(projectId))).thenReturn(List.of());

        List<TaskDto> response = sut.getTasksByProjectId(projectId);

        assertTrue(response.isEmpty());
        verify(jdbcTemplate, times(1)).query(anyString(), any(RowMapper.class), eq(projectId));
    }
}
