package com.han.pwac.pinguins.backend.repository;

import com.han.pwac.pinguins.backend.domain.DTO.GetSkillDto;
import com.han.pwac.pinguins.backend.domain.InviteKey;
import com.han.pwac.pinguins.backend.domain.User;
import com.han.pwac.pinguins.backend.exceptions.NotImplementedException;
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
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InviteKeyDaoTest {
    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private InviteKeyDao sut;

    private static class InviteKeyDaoHelper extends InviteKeyDao {

        public InviteKeyDaoHelper(JdbcTemplate jdbcTemplate) {
            super(jdbcTemplate);
        }

        public static RowMapper<InviteKey> getMapper() {
            return mapper;
        }
    }

    @Test
    public void test_InviteKeyDao_mapper() throws SQLException {
        // Arrange
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("key")).thenReturn("1234");
        when(resultSet.getInt("businessId")).thenReturn(1);
        when(resultSet.getTimestamp("createdAt")).thenReturn(new Timestamp(1));

        // Act
        InviteKey inviteKey = InviteKeyDaoHelper.getMapper().mapRow(resultSet, 0);

        // Assert
        Assertions.assertNotNull(inviteKey);
        Assertions.assertEquals(1, inviteKey.getBusinessId());
        Assertions.assertEquals("1234", inviteKey.getKey());
        Assertions.assertEquals(new Timestamp(1), inviteKey.getDateTime());
    }

    @Test
    public void test_findByKey_valid() {
        // Arrange
        InviteKey inviteKey = new InviteKey("1234", 1, new Timestamp(1));
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq("1234"))).thenReturn(inviteKey);

        // Act
        Optional<InviteKey> response = sut.findByKey("1234");

        // Assert
        Assertions.assertTrue(response.isPresent());
        Assertions.assertEquals(inviteKey, response.get());
    }

    @Test
    public void test_findByUserName_notFound() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq("1234"))).thenThrow(new EmptyResultDataAccessException(1));

        // Act
        Optional<InviteKey> response = sut.findByKey("1234");

        // Assert
        Assertions.assertTrue(response.isEmpty());
    }

    @Test
    public void test_findByUsername_error() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq("1234"))).thenThrow(new DataAccessException("error") {});

        // to check if any other exception actually throws
        // Act + Assert
        Assertions.assertThrows(DataAccessException.class,
                () -> sut.findByKey("1234"));
    }

    @Test
    public void test_deleteWithKey_valid() {
        // Arrange
        when(jdbcTemplate.update(anyString(), eq("1234"))).thenReturn(1);

        // Act
        boolean added = sut.delete("1234");

        // Assert
        Assertions.assertTrue(added);
    }

    @Test
    public void test_deleteWithKey_notAdded() {
        // Arrange
        when(jdbcTemplate.update(anyString(), eq("1234"))).thenReturn(0);

        // Act
        boolean added = sut.delete("1234");

        // Assert
        Assertions.assertFalse(added);
    }

    //region Generic functions can be reused for every dao
    private InviteKey getItem() {
        return new InviteKey("1234", 1, new Timestamp(1));
    }

    private Integer getId() {
        return 1;
    }

    @Test
    public void test_getAll_valid() {
        // Arrange

        // this is tested for when this function is implemented and new tests have to be written
        // Act + Assert
        Assertions.assertThrows(NotImplementedException.class,
                () -> sut.getAll());
    }

    @Test
    public void test_findById_valid() {
        // Arrange

        // this is tested for when this function is implemented and new tests have to be written
        // Act + Assert
        Assertions.assertThrows(NotImplementedException.class,
                () -> sut.findById(getId()));
    }

    @Test
    public void test_add_valid() {
        // Arrange
        when(jdbcTemplate.update(anyString(), anyString(), anyInt())).thenReturn(1);

        // Act
        boolean added = sut.add(getItem());

        // Assert
        Assertions.assertTrue(added);
    }

    @Test
    public void test_add_notAdded() {
        // Arrange
        when(jdbcTemplate.update(anyString(), anyString(), anyInt())).thenReturn(0);

        // Act
        boolean added = sut.add(getItem());

        // Assert
        Assertions.assertFalse(added);
    }

    @Test
    public void test_delete_notImplemented() {
        // Arrange

        // this is tested for when this function is implemented and new tests have to be written
        // Act + Assert
        Assertions.assertThrows(NotImplementedException.class,
                () -> sut.delete(getId()));
    }

    @Test
    public void test_update_notImplemented() {
        // Arrange

        // this is tested for when this function is implemented and new tests have to be written
        // Act + Assert
        Assertions.assertThrows(NotImplementedException.class,
                () -> sut.update(getId(), getItem()));
    }

    @Test
    public void test_selectLastId_notImplemented() {
        // Arrange

        // this is tested for when this function is implemented and new tests have to be written
        // Act + Assert
        Assertions.assertThrows(NotImplementedException.class,
                () -> sut.getLastInsertedId());
    }

    //endregion
}
