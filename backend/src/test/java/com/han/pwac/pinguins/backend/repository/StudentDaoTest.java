package com.han.pwac.pinguins.backend.repository;

import com.han.pwac.pinguins.backend.domain.DTO.FileDto;
import com.han.pwac.pinguins.backend.domain.DTO.StudentRepositoryDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class StudentDaoTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    public void test_studentDao_ValidUpdate() {
        // arrange
        StudentDao studentDao = new StudentDao(jdbcTemplate);

        when(jdbcTemplate.update(anyString(), anyString(), anyString(), anyInt())).thenReturn(1);
        when(jdbcTemplate.update(anyString(), anyString(), anyString(), anyString(), anyInt())).thenReturn(1);

        // act
        boolean updated = studentDao.update(1, new StudentRepositoryDto(1, "1", "name", "description", new FileDto(Optional.of("image.png")), new FileDto(Optional.of("cv.pdf")), "email@email.com"));

        // assert
        Assertions.assertTrue(updated);
    }

    @Test
    public void test_studentDao_InvalidUpdate() {
        // arrange
        StudentDao studentDao = new StudentDao(jdbcTemplate);

        when(jdbcTemplate.update(anyString(), anyString(), anyString(), anyInt())).thenReturn(0);
        when(jdbcTemplate.update(anyString(), anyString(), anyString(), anyString(), anyInt())).thenReturn(1);

        // act
        boolean updated = studentDao.update(1, new StudentRepositoryDto(1, "1", "name", "description", new FileDto(Optional.of("image.png")), new FileDto(Optional.of("cv.pdf")), "email@email.com"));

        // assert
        Assertions.assertFalse(updated);
    }
}
