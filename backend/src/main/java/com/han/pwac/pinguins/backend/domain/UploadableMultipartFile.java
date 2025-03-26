package com.han.pwac.pinguins.backend.domain;

import com.han.pwac.pinguins.backend.domain.contract.UploadableFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class UploadableMultipartFile implements UploadableFile {
    private final MultipartFile file;

    public UploadableMultipartFile(MultipartFile file) {
        this.file = file;
    }

    @Override
    public long getSize() {
        return file.getSize();
    }

    @Override
    public boolean isEmpty() {
        return file.isEmpty();
    }

    @Override
    public String getOriginalFilename() {
        return file.getOriginalFilename();
    }

    @Override
    public byte[] getBytes() throws IOException {
        return file.getBytes();
    }
}
