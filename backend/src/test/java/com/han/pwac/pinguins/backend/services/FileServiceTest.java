package com.han.pwac.pinguins.backend.services;

import com.han.pwac.pinguins.backend.exceptions.BadFileUploadException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileServiceTest {
  @InjectMocks private FileService sut;
  @Mock
  private FileSaveService fileSaveService;
  @Mock private MultipartFile file;

  @Test
  public void uploadImage_successfulAttempt1() {
    // Arrange
    when(fileSaveService.saveFile(any())).thenReturn("406880a7-47d3-408c-b04f-3720301a3f23.jpg");
    when(file.getOriginalFilename()).thenReturn("test.jpg");
    when(file.getContentType()).thenReturn("image/png");

    // Act
    String response = sut.uploadFile(file, new MimeType("image", "*"));

    // Assert
    assertEquals(response, "406880a7-47d3-408c-b04f-3720301a3f23.jpg");
    verify(fileSaveService, times(1)).saveFile(any());
  }

  @Test
  public void uploadImage_successfulAttempt2() {
    // Arrange
    when(fileSaveService.saveFile(any())).thenReturn("406880a7-2222-408c-b04f-3720301a3f23.jpg");
    when(file.getOriginalFilename()).thenReturn("test.png");
    when(file.getContentType()).thenReturn("image/png");

    // Act
    String response = sut.uploadFile(file, new MimeType("image", "*"));

    // Assert
    assertEquals(response, "406880a7-2222-408c-b04f-3720301a3f23.jpg");
    verify(fileSaveService, times(1)).saveFile(any());
  }

  @Test
  public void uploadImage_withDisallowedExtension() {
    // Arrange
    when(file.getOriginalFilename()).thenReturn("test.txt");
    when(file.getContentType()).thenReturn("text/plain");

    // Act & Assert
    assertThrows(
            BadFileUploadException.class,
            () -> sut.uploadFile(file, new MimeType("image", "*"))
    );
  }

  @Test
  public void uploadImage_withNoName() {
    // Arrange
    when(file.getOriginalFilename()).thenReturn(null);
    when(file.getContentType()).thenReturn("text/plain");

    // Act & Assert
    assertThrows(
            BadFileUploadException.class,
            () -> sut.uploadFile(file, new MimeType("image", "*"))
    );
  }
  @Test
  public void uploadFile_withMIMETypeWithoutSubtype() {
    // Arrange
    when(fileSaveService.saveFile(any())).thenReturn("406880a7-47d3-408c-b04f-3720301a3f23.jpg");
    when(file.getOriginalFilename()).thenReturn("test.jpg");
    when(file.getContentType()).thenReturn("image"); // No subtype in MIME type

    // Act
    String response = sut.uploadFile(file, new MimeType("image", "*"));

    // Assert
    assertEquals(response, "406880a7-47d3-408c-b04f-3720301a3f23.jpg"); // Mocked value
    verify(fileSaveService, times(1)).saveFile(any());
  }

  @Test
  public void downloadFile_successful() {
    // Arrange
    when(fileSaveService.saveFile(any())).thenReturn("406880a7-47d3-408c-b04f-3720301a3f23.jpg");

    // Act
    String response = sut.downloadFile(null);

    // Assert
    assertEquals(response, "406880a7-47d3-408c-b04f-3720301a3f23.jpg");
  }
}
