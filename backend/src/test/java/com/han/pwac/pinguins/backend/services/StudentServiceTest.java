package com.han.pwac.pinguins.backend.services;

import com.han.pwac.pinguins.backend.domain.DTO.*;
import com.han.pwac.pinguins.backend.exceptions.BadFileUploadException;
import com.han.pwac.pinguins.backend.exceptions.NotFoundException;
import com.han.pwac.pinguins.backend.exceptions.StudentNotFoundException;
import com.han.pwac.pinguins.backend.repository.SkillDao;
import com.han.pwac.pinguins.backend.repository.StudentDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StudentServiceTest {
  @InjectMocks private StudentService sut;
  @Mock
  private StudentDao studentDao;
  @Mock
  private SkillDao skillDao;

  @Test
  public void getStudentById_successfulAttempt() {
    // Arrange
    when(studentDao.findById(1)).thenReturn(Optional.of(new StudentRepositoryDto(1, "sessionId", "username", "description", new FileDto(Optional.of("profilePicture")), new FileDto(Optional.of("cv")), "email@email.com")));
    when(skillDao.getAllForStudent(1)).thenReturn(new ArrayList<>());

    // Act
    StudentDto response = sut.getStudentById(1);

    // Assert
    assertEquals(1, response.userId());
    assertEquals("username", response.username());
    assertEquals("description", response.description());
    assertEquals("profilePicture", response.profilePicture().path().get());
    assertEquals("cv", response.cv().path().get());
  }

  @Test
  public void getStudentById_studentNotFound() {
    // Arrange
    when(studentDao.findById(1)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
            StudentNotFoundException.class,
            () -> sut.getStudentById(1)
    );
  }

  @Test
  public void editStudentSkillDescription_happy() {
    // Arrange
    when(skillDao.getAllForStudent(1)).thenReturn(List.of(new GetSkillWithDescriptionDto(new GetSkillDto(1, "name", false), "description")));

    // Act
    sut.editStudentSkillDescription(1, new GetSkillWithDescriptionDto(new GetSkillDto(1, "name", false), "description"));

    // Assert
    verify(skillDao, times(1)).editSkillDescription(1, new GetSkillWithDescriptionDto(new GetSkillDto(1, "name", false), "description"));
  }

  @Test
  public void updateStudentSkills_happy() {
    // Arrange
    when(skillDao.getAllForStudent(1)).thenReturn(List.of(
            new GetSkillWithDescriptionDto(new GetSkillDto(1, "name", false), "description"),
            new GetSkillWithDescriptionDto(new GetSkillDto(2, "name2", true), "description2")
    ));
    when(skillDao.findById(3)).thenReturn(Optional.of(new GetSkillDto(3, "name3", true)));

    // Act
    sut.updateStudentSkills(1, List.of(1, 3));

    // Assert
    verify(skillDao, times(1)).removeSkillFromStudent(1, 2);
    verify(skillDao, times(1)).addSkillToStudent(1, 3);
    verify(skillDao, times(1)).getAllForStudent(1);
    verifyNoMoreInteractions(skillDao);
  }

  @Test
  public void updateStudentSkills_skillNotFound() {
    // Arrange
    when(skillDao.getAllForStudent(1)).thenReturn(List.of(
            new GetSkillWithDescriptionDto(new GetSkillDto(1, "name", false), "description"),
            new GetSkillWithDescriptionDto(new GetSkillDto(2, "name2", true), "description2")
    ));
    when(skillDao.findById(3)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
            NotFoundException.class,
            () -> sut.updateStudentSkills(1, List.of(1, 3))
    );
  }
}
