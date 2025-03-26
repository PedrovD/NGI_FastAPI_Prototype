package com.han.pwac.pinguins.backend.repository;

import com.han.pwac.pinguins.backend.domain.DTO.TaskDto;
import com.han.pwac.pinguins.backend.domain.EmailPart;
import com.han.pwac.pinguins.backend.exceptions.NotImplementedException;
import com.han.pwac.pinguins.backend.repository.contract.IBaseDao;
import com.han.pwac.pinguins.backend.services.contract.IMailCronService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TaskDao implements IBaseDao<TaskDto> {
    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<TaskDto> mapper = (rs, rowNum) -> new TaskDto(
            rs.getInt("taskId"),
            rs.getInt("projectId"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getInt("totalNeeded")
    );

    @Autowired
    public TaskDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addTask(TaskDto task) {
        String sql = "INSERT INTO Tasks (projectId, title, description, totalNeeded) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, task.getProjectId(), task.getTitle(), task.getDescription(), task.getTotalNeeded());
    }

    public List<TaskDto> getTasksByProjectId(int projectId) {
        return jdbcTemplate.query("""
                        SELECT taskId, projectId, title, description, totalNeeded
                        FROM Tasks
                        WHERE projectId = ?
                        """,
                mapper,
                projectId
        );
    }
    public Optional<Integer> GetProjectIdFromTask(int taskId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("SELECT projectId FROM Tasks WHERE taskId = ?", Integer.class, taskId));
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<TaskDto> findById(Integer taskId) {
        try {
            String sql = "SELECT * FROM Tasks WHERE taskId = ?";
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, mapper, taskId));
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public List<TaskDto> getByProjectId(int projectId) {
        String sql = "SELECT * FROM Tasks WHERE projectId = ?";
        return jdbcTemplate.query(sql, mapper, projectId);
    }

    @Override
    public Collection<TaskDto> getAll() {
        String sql = "SELECT * FROM Tasks";
        return jdbcTemplate.query(sql, mapper);
    }

    @Override
    public boolean add(TaskDto item) {
        throw new NotImplementedException();
    }

    @Override
    public boolean delete(Integer id) {
        throw new NotImplementedException();
    }

    @Override
    public boolean update(Integer id, TaskDto item) {
        throw new NotImplementedException();
    }

    @Override
    public Integer getLastInsertedId() {
        throw new NotImplementedException();
    }

    public Collection<TaskDto> getAllTasksAfter(Timestamp date) {
        return jdbcTemplate.query("""
                SELECT *
                FROM tasks
                WHERE createdat > ?
                """, mapper, date);
    }
}
