package com.han.pwac.pinguins.backend.validation;

import com.han.pwac.pinguins.backend.domain.DTO.FileDto;
import com.han.pwac.pinguins.backend.domain.DTO.StudentRepositoryDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
public class StudentValidationTest {
    @Test
    public void test_StudentValidation_Valid() {
        // arrange
        FileDto profilePicture = mock(FileDto.class);
        when(profilePicture.isValid()).thenReturn(true);
        FileDto cv = mock(FileDto.class);
        when(cv.isValid()).thenReturn(true);

        StudentRepositoryDto studentDto = new StudentRepositoryDto(
                1,
                "sessionId",
                "name",
                "description",
                profilePicture,
                cv,
                "email@email.com"
        );

        // act
        boolean isValid = studentDto.isValid();

        // assert
        Assertions.assertTrue(isValid);
    }

    @Test
    public void test_StudentValidation_NullValues() {
        // arrange
        FileDto profilePicture = mock(FileDto.class);
        when(profilePicture.isValid()).thenReturn(true);

        StudentRepositoryDto studentDto = new StudentRepositoryDto(
                1,
                "sessionId",
                "name",
                null,
                profilePicture,
                null,
                "email@email.com"
        );

        // act
        boolean isValid = studentDto.isValid();

        // assert
        Assertions.assertFalse(isValid);
    }

    @Test
    public void test_StudentValidation_NullValues2() {
        // arrange
        FileDto cv = mock(FileDto.class);
        when(cv.isValid()).thenReturn(true);

        StudentRepositoryDto studentDto = new StudentRepositoryDto(
                1,
                "sessionId",
                null,
                "desc",
                null,
                cv,
                "email@email.com"
        );

        // act
        boolean isValid = studentDto.isValid();

        // assert
        Assertions.assertFalse(isValid);
    }

    @Test
    public void test_StudentValidation_DescriptionTooLong() {
        // arrange
        FileDto cv = mock(FileDto.class);
        when(cv.isValid()).thenReturn(true);
        FileDto profilePicture = mock(FileDto.class);
        when(profilePicture.isValid()).thenReturn(true);

        StudentRepositoryDto studentDto = new StudentRepositoryDto(
                1,
                "sessionId",
                "name",
                "d".repeat(4001),
                profilePicture,
                cv,
                "email@email.com"
        );

        // act
        boolean isValid = studentDto.isValid();

        // assert
        Assertions.assertFalse(isValid);
    }

    @Test
    public void test_StudentValidation_Description400Chars() {
        // arrange
        FileDto cv = mock(FileDto.class);
        when(cv.isValid()).thenReturn(true);
        FileDto profilePicture = mock(FileDto.class);
        when(profilePicture.isValid()).thenReturn(true);

        StudentRepositoryDto studentDto = new StudentRepositoryDto(
                1,
                "sessionId",
                "name",
                "d".repeat(400),
                profilePicture,
                cv,
                "email@email.com"
        );

        // act
        boolean isValid = studentDto.isValid();

        // assert
        Assertions.assertTrue(isValid);
    }

    @Test
    public void test_StudentValidation_NameTooLong() {
        // arrange
        FileDto cv = mock(FileDto.class);
        when(cv.isValid()).thenReturn(true);
        FileDto profilePicture = mock(FileDto.class);
        when(profilePicture.isValid()).thenReturn(true);

        StudentRepositoryDto studentDto = new StudentRepositoryDto(
                1,
                "sessionId",
                "a".repeat(51),
                "description",
                profilePicture,
                cv,
                "email@email.com"
        );

        // act
        boolean isValid = studentDto.isValid();

        // assert
        Assertions.assertFalse(isValid);
    }

    @Test
    public void test_StudentValidation_Name50Chars() {
        // arrange
        FileDto cv = mock(FileDto.class);
        when(cv.isValid()).thenReturn(true);
        FileDto profilePicture = mock(FileDto.class);
        when(profilePicture.isValid()).thenReturn(true);

        StudentRepositoryDto studentDto = new StudentRepositoryDto(
                1,
                "sessionId",
                "a".repeat(50),
                "description",
                profilePicture,
                cv,
                "email@email.com"
        );

        // act
        boolean isValid = studentDto.isValid();

        // assert
        Assertions.assertTrue(isValid);
    }
}
