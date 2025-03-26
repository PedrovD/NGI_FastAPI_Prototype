package com.han.pwac.pinguins.backend.services.contract;

import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.Optional;

public interface IFileService {
    String uploadFile(MultipartFile file, MimeType validMimeType);
    String downloadFile(URL url);
}
