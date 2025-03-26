package com.han.pwac.pinguins.backend.repository;

import com.han.pwac.pinguins.backend.domain.DTO.FileDto;
import com.han.pwac.pinguins.backend.domain.DTO.StudentRepositoryDto;
import com.han.pwac.pinguins.backend.exceptions.NotImplementedException;
import com.han.pwac.pinguins.backend.repository.contract.IBaseDao;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collection;
import java.util.Optional;

@Repository
public class StudentDao implements IBaseDao<StudentRepositoryDto> {
    private static final RowMapper<StudentRepositoryDto> mapper = (resultSet, rowNum) -> new StudentRepositoryDto(
            resultSet.getInt("userId"),
            resultSet.getString("providerId"),
            resultSet.getString("username"),
            resultSet.getString("description"),
            new FileDto(Optional.ofNullable(resultSet.getString("profilePicture"))),
            new FileDto(Optional.ofNullable(resultSet.getString("cv"))),
            resultSet.getString("email")
    );

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public StudentDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public Collection<StudentRepositoryDto> getAll() {
        return jdbcTemplate.query("""
            SELECT Students.userId, Users.providerId, username, description, imagePath AS profilePicture, CVPath as cv, email
            FROM Students
            INNER JOIN Users
                ON Students.userId = Users.userId
            WHERE Users.email IS NOT NULL
        """, mapper);
    }

    @Override
    public Optional<StudentRepositoryDto> findById(Integer id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("""
                SELECT Students.userId, Users.providerId, username, description, imagePath AS profilePicture, CVPath as cv, email
                FROM Students
                INNER JOIN Users
                    ON Students.userId = Users.userId
                WHERE Students.userId = ? AND Users.email IS NOT NULL
                """, mapper, id));
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public boolean add(StudentRepositoryDto item) {
        boolean updated = jdbcTemplate.update("""
                INSERT INTO Users (providerId, username, imagePath, email)
                    VALUES (?, ?, ?, ?)
                """, item.providerId(), item.username(), item.profilePicture().path().orElse(null), item.email()) != 0;

        Integer lastInsertedId = jdbcTemplate.queryForObject("SELECT MAX(userId) FROM Users", Integer.class);

        boolean updated2 = jdbcTemplate.update("""
                INSERT INTO Students (userId, description, cvPath)
                    VALUES (?, ?, ?)
                """, lastInsertedId, item.description(), item.cv().path().orElse(null)) != 0;

        return updated && updated2;
    }

    @Override
    public boolean delete(Integer integer) {
        throw new NotImplementedException();
    }

    @Transactional
    @Override
    public boolean update(Integer id, StudentRepositoryDto item) {
            boolean updated = jdbcTemplate.update("""
                UPDATE Students
                SET description = ?, CVPath = ?
                WHERE userId = ?
                """, item.description(), item.cv().path().orElse(null), id) != 0;

            boolean updated2 = jdbcTemplate.update("""
                UPDATE Users
                SET username = ?, imagePath = ?, email = ?
                WHERE userId = ?
                """, item.username(), item.profilePicture().path().orElse(null), item.email(), id) != 0;

            return updated && updated2;
    }

    @Override
    public Integer getLastInsertedId() {
        throw new NotImplementedException();
    }
}
