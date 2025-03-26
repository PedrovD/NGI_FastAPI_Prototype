package com.han.pwac.pinguins.backend.validation;

import com.han.pwac.pinguins.backend.domain.DTO.BusinessDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.Mockito.mock;

@SpringBootTest
public class BusinessValidationTest {
    @Test
    public void test_BusinessDto_ValidateNullFields() {
        // arrange
        BusinessDto businessDto = new BusinessDto(1, null, "description", null, "location");

        // act
        boolean validated = businessDto.isValid();

        // assert
        Assertions.assertFalse(validated);
    }

    @Test
    public void test_BusinessDto_ValidateNullFields2() {
        // arrange
        BusinessDto businessDto = new BusinessDto(1, "name", null, null, "location");

        // act
        boolean validated = businessDto.isValid();

        // assert
        Assertions.assertFalse(validated);
    }
}
