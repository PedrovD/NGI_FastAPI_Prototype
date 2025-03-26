package com.han.pwac.pinguins.backend.repository;

import com.han.pwac.pinguins.backend.domain.DTO.BusinessDto;
import com.han.pwac.pinguins.backend.domain.DTO.FileDto;
import com.han.pwac.pinguins.backend.domain.DTO.RegistrationDto;
import com.han.pwac.pinguins.backend.domain.RegistrationId;
import com.han.pwac.pinguins.backend.exceptions.NotFoundException;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BusinessDAOTest {
    @InjectMocks
    private BusinessDao sut;
    @Mock
    private JdbcTemplate jdbcTemplate;

    private static class BusinessDaoHelper extends BusinessDao {

        public BusinessDaoHelper(JdbcTemplate template) {
            super(template);
        }

        public static RowMapper<BusinessDto> getMapper() {
            return mapper;
        }
    }

    @Test
    public void test_InviteKeyDao_mapper() throws SQLException {
        // Arrange
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getInt("businessId")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("name");
        when(resultSet.getString("description")).thenReturn("description");
        when(resultSet.getString("location")).thenReturn("location");
        when(resultSet.getString("photo")).thenReturn("photo.png");

        // Act
        BusinessDto business = BusinessDaoHelper.getMapper().mapRow(resultSet, 0);

        // Assert
        Assertions.assertNotNull(business);
        Assertions.assertEquals(1, business.businessId());
        Assertions.assertEquals("name", business.name());
        Assertions.assertEquals("description", business.description());
        Assertions.assertEquals("location", business.location());
        Assertions.assertEquals("photo.png", business.photo().path().orElse(""));
    }

    @Test
    public void getAll_returnsCollection() {
        // Arrange
        List<BusinessDto> testBusinesses = Arrays.asList(
                new BusinessDto(1, "Business 1", "Test description 1", new FileDto(Optional.empty()), "Location Business 1"),
                new BusinessDto(2, "Business 2", "Test description 2", new FileDto(Optional.empty()), "Location Business 2")
        );
        when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(testBusinesses);

        // Act
        Collection<BusinessDto> testResult = sut.getAll();

        // Assert
        assertEquals(testBusinesses, testResult);
        verify(jdbcTemplate).query(anyString(), any(RowMapper.class));
    }

    @Test
    public void findById_returnsBusiness() {
        // Arrange
        BusinessDto testBusiness = new BusinessDto(1, "Business", "Description", new FileDto(Optional.empty()), "Location");
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(1))).thenReturn(testBusiness);

        // Act
        Optional<BusinessDto> testResult = sut.findById(1);

        // Assert
        assertEquals(testBusiness, testResult.get());
    }

    @Test
    public void findById_doesNotReturnBusiness() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(1))).thenThrow(EmptyResultDataAccessException.class);

        // Act
        Optional<BusinessDto> result = sut.findById(1);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    public void update_successfulAttempt() {
        // Arrange
        BusinessDto testBusiness = new BusinessDto(1, "Business", "Description", new FileDto(Optional.empty()), "Location");
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any())).thenReturn(1);

        // Act
        boolean testResult = sut.update(1, testBusiness);

        // Assert
        assertTrue(testResult);
        verify(jdbcTemplate).update(anyString(), eq(testBusiness.name()), eq(testBusiness.description()),
                isNull(), eq(testBusiness.location()), eq(1));
    }

    @Test
    public void update_failed() {
        // Arrange
        BusinessDto testBusiness = new BusinessDto(1, "Business", "Description", new FileDto(Optional.empty()), "Location");
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any())).thenReturn(0);

        // Act
        boolean result = sut.update(1, testBusiness);

        // Assert
        assertFalse(result);
    }

    @Test
    public void test_addUserToBusiness_valid() {
        // Arrange
        when(jdbcTemplate.update(anyString(), eq(1))).thenReturn(1);
        when(jdbcTemplate.update(anyString(), eq(1), eq(1))).thenReturn(1);

        // Act
        boolean result = sut.addUserToBusiness(1, 1);

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    public void test_addUserToBusiness_notFound() {
        // Arrange
        when(jdbcTemplate.update(anyString(), eq(1))).thenReturn(1);
        when(jdbcTemplate.update(anyString(), eq(1), eq(1))).thenReturn(0);

        // Act
        boolean result = sut.addUserToBusiness(1, 1);

        // Assert
        Assertions.assertFalse(result);
    }

    @Test
    public void test_getByProjectId_valid() {
        // Arrange
        BusinessDto item = getItem();
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(1))).thenReturn(item);

        // Act
        Optional<BusinessDto> response = sut.getByProjectId(1);

        // Assert
        Assertions.assertTrue(response.isPresent());
        Assertions.assertEquals(item, response.get());
    }

    @Test
    public void test_getByProjectId_emptyData() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(1))).thenThrow(new EmptyResultDataAccessException(1));

        // Act
        Optional<BusinessDto> response = sut.getByProjectId(1);

        // Assert
        Assertions.assertTrue(response.isEmpty());
    }

    @Test
    public void test_getByProjectId_error() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(1))).thenThrow(new DataAccessException("error") {});

        // to check if any other exception actually throws
        // Act + Assert
        Assertions.assertThrows(DataAccessException.class,
                () -> sut.getByProjectId(1));
    }

    //region Generic functions can be reused for every dao
    private BusinessDto getItem() {
        return new BusinessDto(1, "name", "description", new FileDto(Optional.of("image.png")), "location");
    }

    private Integer getId() {
        return 1;
    }

    @Test
    public void test_getAll_valid() {
        // Arrange
        List<BusinessDto> items = List.of(getItem());
        when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(items);

        // Act
        Collection<BusinessDto> response = sut.getAll();

        // Assert
        Assertions.assertEquals(items, response);
    }

    @Test
    public void test_findById_valid() {
        // Arrange
        BusinessDto item = getItem();
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), anyInt())).thenReturn(item);

        // Act
        Optional<BusinessDto> response = sut.findById(getId());

        // Assert
        Assertions.assertTrue(response.isPresent());
        Assertions.assertEquals(item, response.get());
    }

    @Test
    public void test_findById_emptyData() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), anyInt())).thenThrow(new EmptyResultDataAccessException(1));

        // Act
        Optional<BusinessDto> response = sut.findById(getId());

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
        when(jdbcTemplate.update(anyString(), anyString(), anyString())).thenReturn(1);

        // Act
        boolean added = sut.add(getItem());

        // Assert
        Assertions.assertTrue(added);
    }

    @Test
    public void test_add_notAdded() {
        // Arrange
        when(jdbcTemplate.update(anyString(), anyString(), anyString())).thenReturn(0);

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
        when(jdbcTemplate.update(anyString(), anyString(), anyString(), anyString(), anyString(), anyInt())).thenReturn(1);

        // Act
        boolean updated = sut.update(getId(), getItem());

        // Assert
        Assertions.assertTrue(updated);
    }

    @Test
    public void test_update_notAdded() {
        // Arrange
        when(jdbcTemplate.update(anyString(), anyString(), anyString(), anyString(), anyString(), anyInt())).thenReturn(0);

        // Act
        boolean updated = sut.update(getId(), getItem());

        // Assert
        Assertions.assertFalse(updated);
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

    //endregion
}
