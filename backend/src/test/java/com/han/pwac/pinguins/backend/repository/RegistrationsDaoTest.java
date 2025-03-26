package com.han.pwac.pinguins.backend.repository;

import com.han.pwac.pinguins.backend.domain.DTO.GetRegistrationDto;
import com.han.pwac.pinguins.backend.domain.DTO.GetSkillDto;
import com.han.pwac.pinguins.backend.domain.DTO.RegistrationDto;
import com.han.pwac.pinguins.backend.domain.InviteKey;
import com.han.pwac.pinguins.backend.domain.RegistrationId;
import com.han.pwac.pinguins.backend.exceptions.DatabaseInsertException;
import com.han.pwac.pinguins.backend.exceptions.NotImplementedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegistrationsDaoTest {

    @InjectMocks
    private RegistrationsDao sut;
    @Mock
    private JdbcTemplate template;

    private static class RegistrationsDaoHelper extends RegistrationsDao {

        public RegistrationsDaoHelper(JdbcTemplate template) {
            super(template);
        }

        public static RowMapper<RegistrationDto> getMapper() {
            return mapper;
        }
    }

    @Test
    public void test_InviteKeyDao_mapper() throws SQLException {
        // Arrange
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getInt("supervisorId")).thenReturn(1);
        when(resultSet.getInt("taskId")).thenReturn(1);
        when(resultSet.getInt("userId")).thenReturn(1);
        when(resultSet.getString("description")).thenReturn("description");
        when(resultSet.getString("accepted")).thenReturn("t");
        when(resultSet.getString("response")).thenReturn("response");

        // Act
        RegistrationDto registration = RegistrationsDaoHelper.getMapper().mapRow(resultSet, 0);

        // Assert
        Assertions.assertNotNull(registration);
        Assertions.assertEquals(1, registration.supervisorId());
        Assertions.assertEquals(1, registration.taskId());
        Assertions.assertEquals(1, registration.userId());
        Assertions.assertEquals("description", registration.description());
        Assertions.assertEquals("response", registration.response());
        Assertions.assertTrue(registration.accepted().orElse(false));
    }

    @Test
    public void test_InviteKeyDao_mapper_falseBool() throws SQLException {
        // Arrange
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getInt("supervisorId")).thenReturn(1);
        when(resultSet.getInt("taskId")).thenReturn(1);
        when(resultSet.getInt("userId")).thenReturn(1);
        when(resultSet.getString("description")).thenReturn("description");
        when(resultSet.getString("accepted")).thenReturn("f");
        when(resultSet.getString("response")).thenReturn("response");

        // Act
        RegistrationDto registration = RegistrationsDaoHelper.getMapper().mapRow(resultSet, 0);

        // Assert
        Assertions.assertNotNull(registration);
        Assertions.assertEquals(1, registration.supervisorId());
        Assertions.assertEquals(1, registration.taskId());
        Assertions.assertEquals(1, registration.userId());
        Assertions.assertEquals("description", registration.description());
        Assertions.assertEquals("response", registration.response());
        Assertions.assertFalse(registration.accepted().orElse(true));
    }

    @Test
    public void test_InviteKeyDao_mapper_nullBool() throws SQLException {
        // Arrange
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getInt("supervisorId")).thenReturn(1);
        when(resultSet.getInt("taskId")).thenReturn(1);
        when(resultSet.getInt("userId")).thenReturn(1);
        when(resultSet.getString("description")).thenReturn("description");
        when(resultSet.getString("accepted")).thenReturn(null);
        when(resultSet.getString("response")).thenReturn("response");

        // Act
        RegistrationDto registration = RegistrationsDaoHelper.getMapper().mapRow(resultSet, 0);

        // Assert
        Assertions.assertNotNull(registration);
        Assertions.assertEquals(1, registration.supervisorId());
        Assertions.assertEquals(1, registration.taskId());
        Assertions.assertEquals(1, registration.userId());
        Assertions.assertEquals("description", registration.description());
        Assertions.assertEquals("response", registration.response());
        Assertions.assertTrue(registration.accepted().isEmpty());
    }

    @Test
    public void add_successfulAttempt() {
        // Arrange
        RegistrationDto registration = new RegistrationDto(1, 1, 5, "Test description", Optional.empty(), "");
        when(template.update(anyString(), eq(registration.taskId()), eq(registration.userId()), eq(registration.description()), eq(null), eq(registration.response()))).thenReturn(1);

        // Act
        boolean response = sut.add(registration);

        // Assert
        assertTrue(response);
        verify(template, times(1)).update(anyString(), eq(registration.taskId()), eq(registration.userId()), eq(registration.description()), eq(null), eq(registration.response()));
    }

    @Test
    public void add_failedAttempt() {
        // Arrange
        RegistrationDto registration = new RegistrationDto(1, 1, 5, "Test description", Optional.empty(), "");
        when(template.update(anyString(), eq(registration.taskId()), eq(registration.userId()), eq(registration.description()), eq(null), eq(registration.response()))).thenReturn(0);

        // Act
        boolean response = sut.add(registration);

        // Assert
        assertFalse(response);
        verify(template, times(1)).update(anyString(), eq(registration.taskId()), eq(registration.userId()), eq(registration.description()), eq(null), eq(registration.response()));
    }

    @Test
    public void add_existingRegistration() {
        // Arrange
        RegistrationDto registration = new RegistrationDto(1, 1, 5, "Test description", Optional.empty(), "Test response");
        when(template.update(anyString(), eq(registration.taskId()), eq(registration.userId()), eq(registration.description()), eq(null), eq(registration.response()))).thenThrow(new DataIntegrityViolationException("Aanmelding bestaat al."));

        // Act & Assert
        DatabaseInsertException exception = assertThrows(DatabaseInsertException.class, () -> sut.add(registration));
        assertEquals("U bent al aangemeld voor deze taak.", exception.getMessage());
    }

    @Test
    public void getRegistrationsForUser_succesfullAttempt() {
        // Arrange
        int userId = 1;
        List<Integer> testTaskIds = Arrays.asList(1, 2, 3);
        when(template.query(anyString(), any(RowMapper.class), eq(userId))).thenReturn(testTaskIds);

        // Act
        Collection<Integer> taskIds = sut.getRegistrationsForUser(userId);

        // Assert
        assertEquals(testTaskIds, taskIds);
        verify(template).query(contains("SELECT taskId FROM TasksRegistrations WHERE userID = ?"), any(RowMapper.class), eq(userId));
    }

    @Test
    public void getRegistrationsForUser_noTaskIds() {
        // Arrange
        int userId = 1;
        List<Integer> noTaskIdsList = Arrays.asList();
        when(template.query(anyString(), any(RowMapper.class), eq(userId))).thenReturn(noTaskIdsList);

        // Act
        Collection<Integer> noTaskIds = sut.getRegistrationsForUser(userId);

        // Assert
        assertEquals(noTaskIdsList, noTaskIds);
        verify(template).query(contains("SELECT taskId FROM TasksRegistrations WHERE userID = ?"), any(RowMapper.class), eq(userId));
    }

    @Test
    public void test_getRegistrationAfter_valid() {
        // Arrange
        Timestamp time = new Timestamp(1);
        List<RegistrationDto> registrations = List.of(getItem());
        when(template.query(anyString(), any(RowMapper.class), eq(time))).thenReturn(registrations);

        // Act
        Collection<RegistrationDto> gotRegistrations = sut.getRegistrationsAfter(time);

        // Assert
        Assertions.assertEquals(registrations, gotRegistrations);
    }

    //region Generic functions can be reused for every dao
    private RegistrationDto getItem() {
        return new RegistrationDto(1, 1, 1, "description", Optional.of(true), "response");
    }

    private RegistrationId getId() {
        return new RegistrationId(1, 1);
    }

    @Test
    public void test_getAll_valid() {
        // Arrange
        List<RegistrationDto> items = List.of(getItem());
        when(template.query(anyString(), any(RowMapper.class))).thenReturn(items);

        // Act
        Collection<RegistrationDto> response = sut.getAll();

        // Assert
        Assertions.assertEquals(items, response);
    }

    @Test
    public void test_findById_valid() {
        // Arrange
        RegistrationDto item = getItem();
        when(template.queryForObject(anyString(), any(RowMapper.class), anyInt(), anyInt())).thenReturn(item);

        // Act
        Optional<RegistrationDto> response = sut.findById(getId());

        // Assert
        Assertions.assertTrue(response.isPresent());
        Assertions.assertEquals(item, response.get());
    }

    @Test
    public void test_findById_emptyData() {
        // Arrange
        when(template.queryForObject(anyString(), any(RowMapper.class), anyInt(), anyInt())).thenThrow(new EmptyResultDataAccessException(1));

        // Act
        Optional<RegistrationDto> response = sut.findById(getId());

        // Assert
        Assertions.assertTrue(response.isEmpty());
    }

    @Test
    public void test_findById_error() {
        // Arrange
        when(template.queryForObject(anyString(), any(RowMapper.class), anyInt(), anyInt())).thenThrow(new DataAccessException("error") {});

        // to check if any other exception actually throws
        // Act + Assert
        Assertions.assertThrows(DataAccessException.class,
                () -> sut.findById(getId()));
    }

    @Test
    public void test_add_valid() {
        // Arrange
        when(template.update(anyString(), anyInt(), anyInt(), anyString(), any(), anyString())).thenReturn(1);

        // Act
        boolean added = sut.add(getItem());

        // Assert
        Assertions.assertTrue(added);
    }

    @Test
    public void test_add_notAdded() {
        // Arrange
        when(template.update(anyString(), anyInt(), anyInt(), anyString(), any(), anyString())).thenReturn(0);

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
    public void test_update_valid() {
        // Arrange
        when(template.update(anyString(), anyString(), anyBoolean(), anyString(), anyInt(), anyInt())).thenReturn(1);

        // Act
        boolean updated = sut.update(getId(), getItem());

        // Assert
        Assertions.assertTrue(updated);
    }

    @Test
    public void test_update_notAdded() {
        // Arrange
        when(template.update(anyString(), anyString(), anyBoolean(), anyString(), anyInt(), anyInt())).thenReturn(0);

        // Act
        boolean updated = sut.update(getId(), getItem());

        // Assert
        Assertions.assertFalse(updated);
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