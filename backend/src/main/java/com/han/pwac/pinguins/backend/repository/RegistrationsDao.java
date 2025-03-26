package com.han.pwac.pinguins.backend.repository;

import com.han.pwac.pinguins.backend.domain.DTO.GetRegistrationDto;
import com.han.pwac.pinguins.backend.domain.EmailPart;
import com.han.pwac.pinguins.backend.domain.RegistrationId;
import com.han.pwac.pinguins.backend.domain.DTO.RegistrationDto;
import com.han.pwac.pinguins.backend.exceptions.DatabaseInsertException;
import com.han.pwac.pinguins.backend.exceptions.NotImplementedException;
import com.han.pwac.pinguins.backend.repository.contract.IRegistrationDao;
import com.han.pwac.pinguins.backend.services.contract.IMailCronService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Repository
public class RegistrationsDao implements IRegistrationDao {
    private final JdbcTemplate template;

    protected static final RowMapper<RegistrationDto> mapper = (resultSet, rowNum) -> new RegistrationDto(
            resultSet.getInt("supervisorId"),
            resultSet.getInt("taskId"),
            resultSet.getInt("userId"),
            resultSet.getString("description"),
            getOptionalBoolean(resultSet, "accepted"),
            resultSet.getString("response")
    );

    @Autowired
    public RegistrationsDao(JdbcTemplate template) {
        this.template = template;
    }

    private static Optional<Boolean> getOptionalBoolean(ResultSet resultSet, String columnLabel) throws SQLException {
        String nullableBoolean = resultSet.getString(columnLabel);
        if (nullableBoolean == null) {
            return Optional.empty();
        }
        return Optional.of(nullableBoolean.equals("t"));
    }

    public Collection<Integer> getRegistrationsForUser(int userId) {
        return template.query("""
                        SELECT taskId FROM TasksRegistrations WHERE userID = ?
                        """,
                (resultSet, rowNum) -> resultSet.getInt("taskId"), userId);
    }

    @Override
    public Collection<RegistrationDto> getAll() {
        return template.query("""
                SELECT taskId, userId, description, accepted, response, (
                    SELECT userId
                    FROM Projects 
                    INNER JOIN Tasks 
                        ON Projects.projectId = Tasks.projectId
                    WHERE Tasks.taskId = TasksRegistrations.taskId 
                ) AS supervisorId
                FROM TasksRegistrations
                """, mapper);
    }

    @Override
    public Optional<RegistrationDto> findById(RegistrationId registrationId) {
        try {
            return Optional.ofNullable(template.queryForObject("""
                           SELECT taskId, userId, description, accepted, response, (
                                SELECT userId 
                                FROM Projects 
                                INNER JOIN Tasks 
                                    ON Projects.projectId = Tasks.projectId
                                WHERE Tasks.taskId = TasksRegistrations.taskId
                           ) AS supervisorId
                           FROM TasksRegistrations 
                           WHERE taskId = ? AND userId = ?
                    """, mapper, registrationId.taskId, registrationId.userId));
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public boolean add(RegistrationDto registration) {
        try {
            return template.update("""
                    INSERT INTO TasksRegistrations (taskId, userId, description, accepted, response)
                    VALUES (?, ?, ?, ?, ?)
                    """, registration.taskId(), registration.userId(), registration.description(), null, registration.response()) != 0;
        } catch (DataIntegrityViolationException dive) {
            throw new DatabaseInsertException("U bent al aangemeld voor deze taak.");
        }
    }

    @Override
    public boolean delete(RegistrationId registrationId) {
        throw new NotImplementedException();
    }

    @Override
    public boolean update(RegistrationId registrationId, RegistrationDto item) {
        return template.update("""
                        UPDATE TasksRegistrations
                        SET description = ?, accepted = ?, response = ?
                        WHERE taskId = ? AND userId = ?
                        """,
                item.description(), item.accepted().orElse(null), item.response(), registrationId.taskId, registrationId.userId) != 0;
    }

    @Override
    public RegistrationId getLastInsertedId() {
        throw new NotImplementedException();
    }

    @Override
    public Collection<RegistrationDto> getRegistrationsAfter(Timestamp date) {
        return template.query("""
                SELECT taskid, userId, description, accepted, response, (
                    SELECT userId
                    FROM Projects
                    INNER JOIN Tasks 
                        ON Projects.projectId = Tasks.projectId
                    WHERE Tasks.taskId = TasksRegistrations.taskId 
                ) AS supervisorId
                FROM TasksRegistrations
                WHERE createdat > ? AND accepted IS NULL
                """, mapper, date);
    }
}
