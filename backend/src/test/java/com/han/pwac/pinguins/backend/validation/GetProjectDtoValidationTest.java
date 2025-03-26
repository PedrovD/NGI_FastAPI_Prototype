package com.han.pwac.pinguins.backend.validation;

import com.han.pwac.pinguins.backend.domain.DTO.BusinessDto;
import com.han.pwac.pinguins.backend.domain.DTO.FileDto;
import com.han.pwac.pinguins.backend.domain.DTO.GetProjectDto;
import com.han.pwac.pinguins.backend.domain.DTO.GetSkillDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

public class GetProjectDtoValidationTest {

    @Test
    public void test_validate_valid() {
        // Arrange

        String title = "my title";
        String description = "my description";
        Collection<GetSkillDto> skill = List.of(new GetSkillDto(1, "name", true));
        FileDto businessPhoto = mock(FileDto.class);
        when(businessPhoto.isValid()).thenReturn(true);

        BusinessDto businessDto = new BusinessDto(1, "name", "description", businessPhoto, "location");
        FileDto photo = mock(FileDto.class);
        when(photo.isValid()).thenReturn(true);

        GetProjectDto projectDto = new GetProjectDto(1, title, description, skill, businessDto, photo);

        // Act
        boolean value = projectDto.isValid();

        // Assert
        Assertions.assertTrue(value);

        verify(photo, times(1)).isValid();
        verify(businessPhoto, times(1)).isValid();
    }

    @Test
    public void test_validate_titleTooLong() {
        // Arrange

        String title = "a".repeat(GetProjectDto.TITLE_LENGTH + 1);
        String description = "my description";
        Collection<GetSkillDto> skill = List.of(new GetSkillDto(1, "name", true));
        FileDto businessPhoto = mock(FileDto.class);
        when(businessPhoto.isValid()).thenReturn(true);

        BusinessDto businessDto = new BusinessDto(1, "name", "description", businessPhoto, "location");
        FileDto photo = mock(FileDto.class);
        when(photo.isValid()).thenReturn(true);

        GetProjectDto projectDto = new GetProjectDto(1, title, description, skill, businessDto, photo);

        // Act
        boolean value = projectDto.isValid();

        // Assert
        Assertions.assertFalse(value);
    }

    @Test
    public void test_validate_titleEqualToMaxLength() {
        // Arrange

        String title = "a".repeat(GetProjectDto.TITLE_LENGTH);
        String description = "my description";
        Collection<GetSkillDto> skill = List.of(new GetSkillDto(1, "name", true));
        FileDto businessPhoto = mock(FileDto.class);
        when(businessPhoto.isValid()).thenReturn(true);

        BusinessDto businessDto = new BusinessDto(1, "name", "description", businessPhoto, "location");
        FileDto photo = mock(FileDto.class);
        when(photo.isValid()).thenReturn(true);

        GetProjectDto projectDto = new GetProjectDto(1, title, description, skill, businessDto, photo);

        // Act
        boolean value = projectDto.isValid();

        // Assert
        Assertions.assertTrue(value);

        verify(photo, times(1)).isValid();
        verify(businessPhoto, times(1)).isValid();
    }

    @Test
    public void test_validate_descriptionTooLong() {
        // Arrange

        String title = "my title";
        String description = "a".repeat(GetProjectDto.DESCRIPTION_LENGTH + 1);
        Collection<GetSkillDto> skill = List.of(new GetSkillDto(1, "name", true));
        FileDto businessPhoto = mock(FileDto.class);
        when(businessPhoto.isValid()).thenReturn(true);

        BusinessDto businessDto = new BusinessDto(1, "name", "description", businessPhoto, "location");
        FileDto photo = mock(FileDto.class);
        when(photo.isValid()).thenReturn(true);

        GetProjectDto projectDto = new GetProjectDto(1, title, description, skill, businessDto, photo);

        // Act
        boolean value = projectDto.isValid();

        // Assert
        Assertions.assertFalse(value);
    }

    @Test
    public void test_validate_descriptionEqualToMaxLength() {
        // Arrange

        String title = "my title";
        String description = "a".repeat(GetProjectDto.DESCRIPTION_LENGTH);
        Collection<GetSkillDto> skill = List.of(new GetSkillDto(1, "name", true));
        FileDto businessPhoto = mock(FileDto.class);
        when(businessPhoto.isValid()).thenReturn(true);

        BusinessDto businessDto = new BusinessDto(1, "name", "description", businessPhoto, "location");
        FileDto photo = mock(FileDto.class);
        when(photo.isValid()).thenReturn(true);

        GetProjectDto projectDto = new GetProjectDto(1, title, description, skill, businessDto, photo);

        // Act
        boolean value = projectDto.isValid();

        // Assert
        Assertions.assertTrue(value);

        verify(photo, times(1)).isValid();
        verify(businessPhoto, times(1)).isValid();
    }

    @Test
    public void test_validate_tooManySkills() {
        // Arrange

        String title = "my title";
        String description = "my description";
        Collection<GetSkillDto> skill = List.of(new GetSkillDto(1, "name", true), new GetSkillDto(1, "name", true), new GetSkillDto(1, "name", true), new GetSkillDto(1, "name", true), new GetSkillDto(1, "name", true), new GetSkillDto(1, "name", true));
        FileDto businessPhoto = mock(FileDto.class);
        when(businessPhoto.isValid()).thenReturn(true);

        BusinessDto businessDto = new BusinessDto(1, "name", "description", businessPhoto, "location");
        FileDto photo = mock(FileDto.class);
        when(photo.isValid()).thenReturn(true);

        GetProjectDto projectDto = new GetProjectDto(1, title, description, skill, businessDto, photo);

        // Act
        boolean value = projectDto.isValid();

        // Assert
        Assertions.assertFalse(value);
    }

    @Test
    public void test_validate_skillsEqualToMaxLength() {
        // Arrange

        String title = "my title";
        String description = "my description";
        Collection<GetSkillDto> skill = List.of(new GetSkillDto(1, "name", true), new GetSkillDto(1, "name", true), new GetSkillDto(1, "name", true), new GetSkillDto(1, "name", true), new GetSkillDto(1, "name", true));
        FileDto businessPhoto = mock(FileDto.class);
        when(businessPhoto.isValid()).thenReturn(true);

        BusinessDto businessDto = new BusinessDto(1, "name", "description", businessPhoto, "location");
        FileDto photo = mock(FileDto.class);
        when(photo.isValid()).thenReturn(true);

        GetProjectDto projectDto = new GetProjectDto(1, title, description, skill, businessDto, photo);

        // Act
        boolean value = projectDto.isValid();

        // Assert
        Assertions.assertTrue(value);


        verify(photo, times(1)).isValid();
        verify(businessPhoto, times(1)).isValid();
    }
}
