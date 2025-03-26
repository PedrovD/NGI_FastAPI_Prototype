package com.han.pwac.pinguins.backend.validation;

import com.han.pwac.pinguins.backend.domain.DTO.BusinessDto;
import com.han.pwac.pinguins.backend.domain.DTO.RegistrationDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest
public class RegistrationValidationTest {
    @Test
    public void test_RegistrationDto_valid() {
        // arrange
        RegistrationDto registrationDto = new RegistrationDto(1, 1, 1, "mijn aanmelding beschrijving", Optional.empty(), "aanmelding reactie");

        // act
        boolean validated = registrationDto.isValid();

        // assert
        Assertions.assertTrue(validated);
    }

    @Test
    public void test_RegistrationDto_descriptionNull() {
        // arrange
        RegistrationDto registrationDto = new RegistrationDto(1, 1, 1, null, Optional.empty(), "");

        // act
        boolean validated = registrationDto.isValid();

        // assert
        Assertions.assertFalse(validated);
    }

    @Test
    public void test_RegistrationDto_acceptedNull() {
        // arrange
        RegistrationDto registrationDto = new RegistrationDto(1, 1, 1,"mijn aanmelding beschrijving", null, "aanmelding reactie");

        // act
        boolean validated = registrationDto.isValid();

        // assert
        Assertions.assertFalse(validated);
    }

    @Test
    public void test_RegistrationDto_descriptionTooLong() {
        // arrange
        RegistrationDto registrationDto = new RegistrationDto(1, 1, 1, "a".repeat(4001), Optional.of(true), "aanmelding reactie");

        // act
        boolean validated = registrationDto.isValid();

        // assert
        Assertions.assertFalse(validated);
    }

    @Test
    public void test_RegistrationDto_descriptionBarelyInRange() {
        // arrange
        RegistrationDto registrationDto = new RegistrationDto(1, 1, 1, "a".repeat(400), Optional.of(false), "");

        // act
        boolean validated = registrationDto.isValid();

        // assert
        Assertions.assertTrue(validated);
    }
}
