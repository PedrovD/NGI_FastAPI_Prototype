package com.han.pwac.pinguins.backend.repository;

import com.han.pwac.pinguins.backend.domain.DTO.TeacherDto;
import com.han.pwac.pinguins.backend.exceptions.NotImplementedException;
import com.han.pwac.pinguins.backend.repository.contract.ITeacherDao;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class TeacherDao implements ITeacherDao {
    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<TeacherDto> mapper = (resultSet, rowNum) -> new TeacherDto(
            resultSet.getInt("userId")
    );

    public TeacherDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<TeacherDto> getAll() {
        throw new NotImplementedException();
    }

    @Override
    public Optional<TeacherDto> findById(Integer integer) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("SELECT * FROM Teachers WHERE userId = ?", mapper, integer));
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public void addTeacher(Integer userId) {
        jdbcTemplate.update("DELETE FROM Students where userId = ?", userId);
        String sql = "INSERT INTO Teachers (userId) VALUES (?)";
        jdbcTemplate.update(sql, userId);
    }

    @Override
    public boolean add(TeacherDto item) {
        throw new NotImplementedException();
    }

    @Override
    public boolean delete(Integer integer) {
        throw new NotImplementedException();
    }

    @Override
    public boolean update(Integer integer, TeacherDto item) {
        throw new NotImplementedException();
    }

    @Override
    public Integer getLastInsertedId() {
        throw new NotImplementedException();
    }
}
