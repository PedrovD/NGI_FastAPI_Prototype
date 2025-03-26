package com.han.pwac.pinguins.backend.repository;

import com.han.pwac.pinguins.backend.domain.InviteKey;
import com.han.pwac.pinguins.backend.exceptions.NotImplementedException;
import com.han.pwac.pinguins.backend.repository.contract.IInviteKeyDao;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class InviteKeyDao implements IInviteKeyDao {
    private final JdbcTemplate jdbcTemplate;

    protected static final RowMapper<InviteKey> mapper = (resultSet, rowNum) -> new InviteKey(
            resultSet.getString("key"),
            resultSet.getInt("businessId"),
            resultSet.getTimestamp("createdAt")
    );

    public InviteKeyDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public Collection<InviteKey> getAll() {
        throw new NotImplementedException();
    }

    @Override
    public Optional<InviteKey> findById(Integer integer) {
        throw new NotImplementedException();
    }

    public Optional<InviteKey> findByKey(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("SELECT * FROM InviteKeys WHERE key = ?", mapper, id));
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public boolean add(InviteKey item) {
        String sql = "INSERT INTO InviteKeys (key, businessId) VALUES (?, ?)";
        return jdbcTemplate.update(sql, item.getKey(), item.getBusinessId()) > 0;
    }

    @Override
    public boolean delete(Integer integer) {
        throw new NotImplementedException();
    }

    public boolean delete(String code) {
        String sql = "DELETE FROM InviteKeys WHERE key = ?";
        return jdbcTemplate.update(sql, code) > 0;
    }

    @Override
    public boolean update(Integer integer, InviteKey item) {
        throw new NotImplementedException();
    }

    @Override
    public Integer getLastInsertedId() {
        throw new NotImplementedException();
    }
}
