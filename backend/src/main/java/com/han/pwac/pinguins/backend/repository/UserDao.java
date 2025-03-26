package com.han.pwac.pinguins.backend.repository;

import com.han.pwac.pinguins.backend.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    protected static final RowMapper<User> userRowMapper = (rs, RowNum) -> {
        User user = new User();
        user.setProviderId(rs.getString("providerId"));
        user.setId(rs.getInt("userId"));
        user.setUsername(rs.getString("username"));
        user.setImagePath(rs.getString("imagePath"));
        user.setEmail(rs.getString("email"));
        return user;
    };

    public Optional<User> findById(Integer id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("SELECT * FROM Users WHERE userId = ?", userRowMapper, id));
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public Optional<User> getByProviderId(String providerId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("SELECT * FROM Users WHERE providerId = ?", userRowMapper, providerId));
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public boolean setEmail(int userId, String email) {
        return jdbcTemplate.update("""
                UPDATE Users
                SET email = ?
                WHERE userid = ?
                """, email, userId) != 0;
    }
}
