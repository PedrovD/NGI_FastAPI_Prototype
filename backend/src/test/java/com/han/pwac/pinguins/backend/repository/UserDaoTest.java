package com.han.pwac.pinguins.backend.repository;

import com.han.pwac.pinguins.backend.domain.DTO.GetSkillDto;
import com.han.pwac.pinguins.backend.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserDaoTest {
    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private UserDao sut;

    private class UserDaoHelper extends UserDao {

        public UserDaoHelper(JdbcTemplate jdbcTemplate) {
            super(jdbcTemplate);
        }

        public static RowMapper<User> getMapper() {
            return userRowMapper;
        }
    }

    @Test
    public void test_UserDao_mapper() throws SQLException {
        // Arrange
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getInt("userId")).thenReturn(1);
        when(resultSet.getString("providerId")).thenReturn("1234");
        when(resultSet.getString("username")).thenReturn("name");
        when(resultSet.getString("imagePath")).thenReturn("image.png");
        when(resultSet.getString("email")).thenReturn("email@email.com");

        // Act
        User user = UserDaoHelper.getMapper().mapRow(resultSet, 0);

        // Assert
        Assertions.assertNotNull(user);
        Assertions.assertEquals(1, user.getId());
        Assertions.assertEquals("email@email.com", user.getEmail());
        Assertions.assertEquals("name", user.getUsername());
        Assertions.assertEquals("image.png", user.getImagePath());
        Assertions.assertEquals("1234", user.getProviderId());
    }

    @Test
    public void test_findById_valid() {
        // Arrange
        User user = new User(1, "username", "image.png", "email@email.com");
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(1))).thenReturn(user);

        // Act
        Optional<User> response = sut.findById(1);

        // Assert
        Assertions.assertTrue(response.isPresent());
        Assertions.assertEquals(user, response.get());
    }

    @Test
    public void test_findById_notFound() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(1))).thenThrow(new EmptyResultDataAccessException(1));

        // Act
        Optional<User> response = sut.findById(1);

        // Assert
        Assertions.assertTrue(response.isEmpty());
    }

    @Test
    public void test_findById_error() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(1))).thenThrow(new DataAccessException("error") {});

        // to check if any other exception actually throws
        // Act + Assert
        Assertions.assertThrows(DataAccessException.class,
                () -> sut.findById(1));
    }

    @Test
    public void test_getByProviderId_valid() {
        // Arrange
        User user = new User(1, "username", "image.png", "email@email.com");
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq("1234"))).thenReturn(user);

        // Act
        Optional<User> response = sut.getByProviderId("1234");

        // Assert
        Assertions.assertTrue(response.isPresent());
        Assertions.assertEquals(user, response.get());
    }

    @Test
    public void test_getByProviderId_notFound() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq("1234"))).thenThrow(new EmptyResultDataAccessException(1));

        // Act
        Optional<User> response = sut.getByProviderId("1234");

        // Assert
        Assertions.assertTrue(response.isEmpty());
    }

    @Test
    public void test_getByProviderId_error() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq("1234"))).thenThrow(new DataAccessException("error") {});

        // to check if any other exception actually throws
        // Act + Assert
        Assertions.assertThrows(DataAccessException.class,
                () -> sut.getByProviderId("1234"));
    }

    @Test
    public void test_setEmail_valid() {
        // Arrange
        when(jdbcTemplate.update(anyString(), eq("email@email.com"), eq(1))).thenReturn(1);

        // Act
        boolean response = sut.setEmail(1, "email@email.com");

        // Assert
        Assertions.assertTrue(response);
    }

    @Test
    public void test_setEmail_notFound() {
        // Arrange
        when(jdbcTemplate.update(anyString(), eq("email@email.com"), eq(1))).thenReturn(0);

        // Act
        boolean response = sut.setEmail(1, "email@email.com");

        // Assert
        Assertions.assertFalse(response);
    }
}
