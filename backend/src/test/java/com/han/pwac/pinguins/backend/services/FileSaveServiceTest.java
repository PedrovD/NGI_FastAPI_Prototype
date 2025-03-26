package com.han.pwac.pinguins.backend.services;

import com.han.pwac.pinguins.backend.domain.UploadableMultipartFile;
import com.han.pwac.pinguins.backend.exceptions.BadFileUploadException;
import com.han.pwac.pinguins.backend.exceptions.InternalFileUploadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileSaveServiceTest {

  private FileSaveService fileSaveService;
  private MultipartFile multipartFile;

  @BeforeEach
  void setUp() {
    fileSaveService = new FileSaveService();
    multipartFile = mock(MultipartFile.class);
  }

  @Test
  void testSaveFile_Success() throws IOException {
    // Arrange
    when(multipartFile.getOriginalFilename()).thenReturn("test.txt");
    when(multipartFile.getSize()).thenReturn(1024L); // L staat erachter omdat het een Long moet zijn
    when(multipartFile.isEmpty()).thenReturn(false);
    when(multipartFile.getBytes()).thenReturn("Test content".getBytes());

    // Act
    String savedFilePath = fileSaveService.saveFile(new UploadableMultipartFile(multipartFile));
    Path filePath = Path.of("src/main/resources/static/files" + savedFilePath);

    // Assert
    assertTrue(Files.exists(filePath));

    // Cleanup
    Files.deleteIfExists(filePath);
  }

  @Test
  void saveFile_FileTooLarge() {
    // Arrange
    when(multipartFile.getSize()).thenReturn(11 * 1024 * 1024L); // 11MB

    // Act & Assert
    assertThrows(BadFileUploadException.class, () -> fileSaveService.saveFile(new UploadableMultipartFile(multipartFile)));
  }

  @Test
  void saveFile_FileExactly10MB() throws IOException {
    // Arrange
    when(multipartFile.getOriginalFilename()).thenReturn("test.txt");
    when(multipartFile.getSize()).thenReturn(10 * 1024 * 1024L);
    when(multipartFile.isEmpty()).thenReturn(false);
    when(multipartFile.getBytes()).thenReturn("Test content".getBytes());

    // Act
    String savedFilePath = fileSaveService.saveFile(new UploadableMultipartFile(multipartFile));
    Path filePath = Path.of("src/main/resources/static/files" + savedFilePath);

    // Assert
    assertTrue(Files.exists(filePath));

    // Cleanup
    Files.deleteIfExists(filePath);
  }

  @Test
  void saveFile_EmptyFile() {
    // Arrange
    when(multipartFile.isEmpty()).thenReturn(true);

    // Act & Assert
    assertThrows(BadFileUploadException.class, () -> fileSaveService.saveFile(new UploadableMultipartFile(multipartFile)));
  }

  @Test
  void saveFile_InternalError() throws IOException {
    // Arrange
    when(multipartFile.getOriginalFilename()).thenReturn("test.txt");
    when(multipartFile.getSize()).thenReturn(1024L);
    when(multipartFile.isEmpty()).thenReturn(false);
    when(multipartFile.getBytes()).thenThrow(new IOException("Test exception"));

    // Act & Assert
    assertThrows(InternalFileUploadException.class, () -> fileSaveService.saveFile(new UploadableMultipartFile(multipartFile)));
  }
}