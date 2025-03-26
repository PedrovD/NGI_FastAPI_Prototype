package com.han.pwac.pinguins.backend.services;

import com.han.pwac.pinguins.backend.domain.DTO.FileDto;
import com.han.pwac.pinguins.backend.domain.DTO.ProjectDto;
import com.han.pwac.pinguins.backend.repository.ProjectDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BaseServiceTest {
  private BaseService<ProjectDto> sut;
  @Mock
  private ProjectDao projectDao;
  @Mock
  private ProjectDto projectDto;

  @BeforeEach
  void setUp() {
    sut = new BaseService<>(projectDao);
  }

  @Test
  public void getAll_successful() {
    // Arrange
    ArrayList<ProjectDto> projects = new ArrayList<>();
    projects.add(new ProjectDto(1, "hallo", "hallo2", new FileDto(Optional.of("hallo.png"))));
    when(projectDao.getAll()).thenReturn(projects);

    // Act
    Collection<ProjectDto> response = sut.getAll();

    // Assert
    assertEquals(projects, response);
  }

  @Test
  public void findById_successful() {
    // Arrange
    ProjectDto project = new ProjectDto(1, "hallo", "hallo2", new FileDto(Optional.of("hallo.png")));
    when(projectDao.findById(1)).thenReturn(Optional.of(project));

    // Act
    ProjectDto response = sut.findById(1).get();

    // Assert
    assertEquals(project, response);
  }

  @Test
  public void update_successful() {
    // Arrange
    when(projectDto.isValid()).thenReturn(true);
    when(projectDao.update(1, projectDto)).thenReturn(true);

    // Act
    boolean response = sut.update(1, projectDto);

    // Assert
    assertTrue(response);
  }

  @Test
  public void update_DAOReturnsFalse() {
    // Arrange
    when(projectDto.isValid()).thenReturn(true);
    when(projectDao.update(1, projectDto)).thenReturn(false);

    // Act
    boolean response = sut.update(1, projectDto);

    // Assert
    assertFalse(response);
  }

  @Test
  public void update_invalid() {
    // Arrange
    when(projectDto.isValid()).thenReturn(false);

    // Act
    boolean response = sut.update(1, projectDto);

    // Assert
    assertFalse(response);
  }

  @Test
  public void delete_successful() {
    // Arrange
    when(projectDao.delete(1)).thenReturn(true);

    // Act
    boolean response = sut.delete(1);

    // Assert
    assertTrue(response);
  }

  @Test
  public void delete_DAOReturnsFalse() {
    // Arrange
    when(projectDao.delete(1)).thenReturn(false);

    // Act
    boolean response = sut.delete(1);

    // Assert
    assertFalse(response);
  }

  @Test
  public void add_successful() {
    // Arrange
    when(projectDto.isValid()).thenReturn(true);
    when(projectDao.add(projectDto)).thenReturn(true);

    // Act
    boolean response = sut.add(projectDto);

    // Assert
    assertTrue(response);
  }

  @Test
  public void add_DAOReturnsFalse() {
    // Arrange
    when(projectDto.isValid()).thenReturn(true);
    when(projectDao.add(projectDto)).thenReturn(false);

    // Act
    boolean response = sut.add(projectDto);

    // Assert
    assertFalse(response);
  }

  @Test
  public void add_invalid() {
    // Arrange
    when(projectDto.isValid()).thenReturn(false);

    // Act
    boolean response = sut.add(projectDto);

    // Assert
    assertFalse(response);
  }
}
