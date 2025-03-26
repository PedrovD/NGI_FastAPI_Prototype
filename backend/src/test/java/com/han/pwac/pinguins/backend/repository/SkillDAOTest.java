package com.han.pwac.pinguins.backend.repository;

import com.han.pwac.pinguins.backend.domain.DTO.GetSkillDto;
import com.han.pwac.pinguins.backend.domain.DTO.GetSkillWithDescriptionDto;
import com.han.pwac.pinguins.backend.exceptions.NotImplementedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class SkillDAOTest {
    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private SkillDao sut;

    private static class SkillDaoHelper extends SkillDao {

        public SkillDaoHelper(JdbcTemplate jdbcTemplate) {
            super(jdbcTemplate);
        }

        public static RowMapper<GetSkillDto> getSkillDtoRowMapper() {
            return SkillDao.mapper;
        }

        public static RowMapper<GetSkillWithDescriptionDto> getSkillDtoRowDescriptionMapper() {
            return SkillDao.skillDescriptionMapper;
        }
    }


    @Test
    public void getTaskSkills_successfulAttempt() {
        // arrange
        int taskId = 1;
        List<GetSkillDto> skills = List.of(new GetSkillDto(1, "Skill Name", false));
        lenient().when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(taskId))).thenReturn(skills);

        // act
        Collection<GetSkillDto> response = sut.getTaskSkills(taskId);

        // assert
        assertEquals(skills, response);
        verify(jdbcTemplate, times(1)).query(anyString(), any(RowMapper.class), eq(taskId));
    }

    @Test
    public void getTaskSkills_noSkillsFound() {
        // arrange
        int taskId = 1;
        lenient().when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(taskId))).thenReturn(List.of());

        // act
        List<GetSkillDto> response = sut.getTaskSkills(taskId);

        // assert
        assertTrue(response.isEmpty());
        verify(jdbcTemplate, times(1)).query(anyString(), any(RowMapper.class), eq(taskId));
    }

    @Test
    public void test_existsTaskSkill_exists() {
        // Arrange
        int taskId = 1;
        when(jdbcTemplate.queryForObject(anyString(), any(Class.class), anyInt(), anyInt())).thenReturn(true);

        // Act
        boolean response = sut.existsTaskSkill(taskId, 1);

        // Assert
        Assertions.assertTrue(response);
    }

    @Test
    public void test_existsTaskSkill_notExists() {
        // Arrange
        int taskId = 1;
        when(jdbcTemplate.queryForObject(anyString(), any(Class.class), anyInt(), anyInt())).thenReturn(false);

        // Act
        boolean response = sut.existsTaskSkill(taskId, 1);

        // Assert
        Assertions.assertFalse(response);
    }

    @Test
    public void test_SkillDao_mapper() throws SQLException {
        // Arrange

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("name");
        when(resultSet.getBoolean("isPending")).thenReturn(true);

        // Act
        GetSkillDto skillDto = SkillDaoHelper.getSkillDtoRowMapper().mapRow(resultSet, 0);

        // Assert
        Assertions.assertNotNull(skillDto);
        Assertions.assertEquals(1, skillDto.skillId());
        Assertions.assertEquals("name", skillDto.name());
        assertTrue(skillDto.isPending());
    }

    @Test
    public void test_SkillDao_descriptionMapper() throws SQLException {
        // Arrange
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("name");
        when(resultSet.getBoolean("isPending")).thenReturn(true);
        when(resultSet.getString("description")).thenReturn("description");

        // Act
        GetSkillWithDescriptionDto skillDto = SkillDaoHelper.getSkillDtoRowDescriptionMapper().mapRow(resultSet, 0);

        // Assert
        Assertions.assertNotNull(skillDto);
        Assertions.assertEquals(1, skillDto.skill().skillId());
        Assertions.assertEquals("name", skillDto.skill().name());
        assertTrue(skillDto.skill().isPending());
        Assertions.assertEquals("description", skillDto.description());
    }

    @Test
    public void test_getTopByProject_valid() {
        // Arrange
        List<GetSkillDto> actualSkills = List.of(new GetSkillDto(1, "name", true));
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(1))).thenReturn(actualSkills);

        // Act
        List<GetSkillDto> gotSkills = sut.getTopByProjectId(1);

        // Assert
        Assertions.assertEquals(actualSkills, gotSkills);
    }

    @Test
    public void test_getAllForStudent_valid() {
        // Arrange
        List<GetSkillWithDescriptionDto> actualSkills = List.of(new GetSkillWithDescriptionDto(new GetSkillDto(1, "name", true), "description"));
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(1))).thenReturn(actualSkills);

        // Act
        Collection<GetSkillWithDescriptionDto> gotSkills = sut.getAllForStudent(1);

        // Assert
        Assertions.assertEquals(actualSkills, gotSkills);
    }

    @Test
    public void test_getAllSkillsAfter_valid() {
        // Arrange
        Timestamp time = new Timestamp(0);
        List<GetSkillDto> actualSkills = List.of(new GetSkillDto(1, "name", true));
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(time))).thenReturn(actualSkills);

        // Act
        Collection<GetSkillDto> gotSkills = sut.getAllSkillsCreatedAfter(time);

        // Assert
        Assertions.assertEquals(actualSkills, gotSkills);
    }

    @Test
    public void test_getTopSkillsForBusiness() {
        // Arrange
        List<GetSkillDto> actualSkills = List.of(new GetSkillDto(1, "name", true));
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(1), eq(5))).thenReturn(actualSkills);

        // Act
        Collection<GetSkillDto> gotSkills = sut.getTopSkillsForBusiness(1, 5);

        // Assert
        Assertions.assertEquals(actualSkills, gotSkills);
    }

    @Test
    public void test_getAllForTask() {
        // Arrange
        List<GetSkillDto> actualSkills = List.of(new GetSkillDto(1, "name", true));
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(1))).thenReturn(actualSkills);

        // Act
        Collection<GetSkillDto> gotSkills = sut.getAllForTask(1);

        // Assert
        Assertions.assertEquals(actualSkills, gotSkills);
    }

    //region Generic functions can be reused for every dao
    private GetSkillDto getItem() {
        return new GetSkillDto(1, "name", true);
    }

    private Integer getId() {
        return 1;
    }

    @Test
    public void test_getAll_valid() {
        // Arrange
        List<GetSkillDto> items = List.of(getItem());
        when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(items);

        // Act
        Collection<GetSkillDto> response = sut.getAll();

        // Assert
        Assertions.assertEquals(items, response);
    }

    @Test
    public void test_findById_valid() {
        // Arrange
        GetSkillDto item = getItem();
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), anyInt())).thenReturn(item);

        // Act
        Optional<GetSkillDto> response = sut.findById(getId());

        // Assert
        Assertions.assertTrue(response.isPresent());
        Assertions.assertEquals(item, response.get());
    }

    @Test
    public void test_findById_emptyData() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), anyInt())).thenThrow(new EmptyResultDataAccessException(1));

        // Act
        Optional<GetSkillDto> response = sut.findById(getId());

        // Assert
        Assertions.assertTrue(response.isEmpty());
    }

    @Test
    public void test_findById_error() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), anyInt())).thenThrow(new DataAccessException("error") {});

        // to check if any other exception actually throws
        // Act + Assert
        Assertions.assertThrows(DataAccessException.class,
                () -> sut.findById(getId()));
    }

    @Test
    public void test_add_valid() {
        // Arrange
        when(jdbcTemplate.update(anyString(), anyString())).thenReturn(1);

        // Act
        boolean added = sut.add(getItem());

        // Assert
        Assertions.assertTrue(added);
    }

    @Test
    public void test_add_notAdded() {
        // Arrange
        when(jdbcTemplate.update(anyString(), anyString())).thenReturn(0);

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
    public void test_selectLastId_found() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(Class.class))).thenReturn(1);

        // Act
        Integer value = sut.getLastInsertedId();

        // Assert
        Assertions.assertEquals(1, value);
    }

    @Test
    public void test_selectLastId_emptyData() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(Class.class))).thenThrow(new EmptyResultDataAccessException(1));

        // Act
        Integer response = sut.getLastInsertedId();

        // Assert
        Assertions.assertEquals(0, response);
    }

    @Test
    public void test_selectLastId_error() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(Class.class))).thenThrow(new DataAccessException("error") {});

        // to check if any other exception actually throws
        // Act + Assert
        Assertions.assertThrows(DataAccessException.class,
                () -> sut.getLastInsertedId());
    }

    //endregion
}
