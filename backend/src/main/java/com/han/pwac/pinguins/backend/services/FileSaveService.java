package com.han.pwac.pinguins.backend.services;

import com.han.pwac.pinguins.backend.domain.contract.UploadableFile;
import com.han.pwac.pinguins.backend.exceptions.BadFileUploadException;
import com.han.pwac.pinguins.backend.exceptions.InternalFileUploadException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileSaveService {
  // Returns path to the saved file
  public String saveFile(UploadableFile file) {
    int maxFileSize = 10 * 1024 * 1024; // 10MB
    if (file.getSize() > maxFileSize) {
      throw new BadFileUploadException("Upload een bestand kleiner dan 10MB");
    }

    if (file.isEmpty()) {
      throw new BadFileUploadException("Upload een geldig, niet leeg bestand");
    }

    try {
      String originalFilename = file.getOriginalFilename();
      String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
      String uploadDir = "src/main/resources/static/files/";

      String randomUuid = java.util.UUID.randomUUID().toString();
      String fileName = randomUuid + extension;
      Path filePath = Paths.get(uploadDir + fileName);

      Files.write(filePath, file.getBytes());
//      return filePath.toString();
      return "/" + fileName;
    } catch (IOException e) {
      throw new InternalFileUploadException("Er is iets misgegaan bij het opslaan van het bestand, probeer later opnieuw");
    }
  }
}
