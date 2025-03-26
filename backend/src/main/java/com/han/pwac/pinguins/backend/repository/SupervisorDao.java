package com.han.pwac.pinguins.backend.repository;

import com.han.pwac.pinguins.backend.domain.DTO.SupervisorDto;
import com.han.pwac.pinguins.backend.exceptions.NotImplementedException;
import com.han.pwac.pinguins.backend.repository.contract.IBaseDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class SupervisorDao implements IBaseDao<SupervisorDto> {
    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<SupervisorDto> mapper = (rs, rowNum) -> {
        SupervisorDto supervisor = new SupervisorDto();
        supervisor.setSupervisorId(rs.getInt("userId"));
        supervisor.setBusinessId(rs.getInt("businessId"));
        return supervisor;
    };

    @Autowired
    public SupervisorDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<SupervisorDto> findById(Integer userId) {
        try {
            String sql = "SELECT * FROM Supervisors WHERE userId = ?";
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, mapper, userId));
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public boolean add(SupervisorDto item) {
        throw new NotImplementedException();
    }

    @Override
    public boolean delete(Integer id) {
        throw new NotImplementedException();
    }

    @Override
    public boolean update(Integer id, SupervisorDto item) {
        throw new NotImplementedException();
    }

    @Override
    public Integer getLastInsertedId() {
        throw new NotImplementedException();
    }

    public List<SupervisorDto> getByBusinessId(int businessId) {
        String sql = "SELECT * FROM Supervisors WHERE businessId = ?";
        return jdbcTemplate.query(sql, mapper, businessId);
    }

    @Override
    public Collection<SupervisorDto> getAll() {
        String sql = "SELECT * FROM Supervisors";
        return jdbcTemplate.query(sql, mapper);
    }

    public String[] getSupervisorEmails(int businessId) {
        String sql = """
            SELECT email
            FROM Users
            INNER JOIN Supervisors ON Users.userId = Supervisors.userId
            WHERE businessId = ?
        """;
        return jdbcTemplate.queryForList(sql, String.class, businessId).toArray(new String[0]);
    }
}
