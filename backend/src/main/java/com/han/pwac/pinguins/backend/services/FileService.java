package com.han.pwac.pinguins.backend.services;

import com.han.pwac.pinguins.backend.domain.UploadableMultipartFile;
import com.han.pwac.pinguins.backend.domain.UploadableUrlFile;
import com.han.pwac.pinguins.backend.exceptions.BadFileUploadException;
import com.han.pwac.pinguins.backend.services.contract.IFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;

@Service
@Primary
public class FileService implements IFileService {
    private final FileSaveService fileSaveService;

    @Autowired
    public FileService(FileSaveService fileSaveService) {
        this.fileSaveService = fileSaveService;
    }

    @Override
    public String uploadFile(MultipartFile file, MimeType allowedMimeType) {
        if (file.getContentType() == null || file.getOriginalFilename() == null) {
            throw new BadFileUploadException("Geen bestandsnaam gevonden.");
        }

        String[] split = file.getContentType().split("/");
        MimeType mime = new MimeType(split[0], split.length > 1 ? split[1] : "*");

        String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        if (!mime.isCompatibleWith(allowedMimeType)) {
            throw new BadFileUploadException("Bestand met de extensie " + extension + " zijn niet toegestaan.");
        }

        return fileSaveService.saveFile(new UploadableMultipartFile(file));
    }

    @Override
    public String downloadFile(URL url) {
        return fileSaveService.saveFile(new UploadableUrlFile(url));
    }
}
