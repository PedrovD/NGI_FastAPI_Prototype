package com.han.pwac.pinguins.backend.domain;

import com.han.pwac.pinguins.backend.domain.contract.UploadableFile;
import com.han.pwac.pinguins.backend.exceptions.InternalFileUploadException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class UploadableUrlFile implements UploadableFile {
    private final URL url;
    private byte[] buffer;

    public UploadableUrlFile(URL url) {
        this.url = url;
    }


    @Override
    public long getSize() {
        try {
            return getBuffer().length;
        } catch (IOException e) {
            throw new InternalFileUploadException("Er is iets misgegaan bij het opslaan van het bestand, probeer later opnieuw");
        }
    }

    @Override
    public boolean isEmpty() {
        return getSize() == 0;
    }

    @Override
    public String getOriginalFilename() {
        try {
            String contentType = url.openConnection().getContentType();
            String extension = MimeTypes.getDefaultMimeTypes().forName(contentType).getExtension();
            return "file" + extension;
        } catch (IOException | MimeTypeException ignored) {
            throw new InternalFileUploadException("Er is iets misgegaan bij het opslaan van het bestand, probeer later opnieuw");
        }
    }

    @Override
    public byte[] getBytes() throws IOException {
        return getBuffer();
    }

    private byte[] getBuffer() throws IOException {
        if (buffer != null) {
            return buffer;
        }

        byte[] buffer = new byte[8096];
        ByteArrayOutputStream arrayStream = new ByteArrayOutputStream();
        try (InputStream stream = url.openStream(); BufferedOutputStream outputStream = new BufferedOutputStream(arrayStream)) {
            int readBytes;
            while ((readBytes = stream.read(buffer)) != -1)
            {
                outputStream.write(buffer, 0, readBytes);
            }

            outputStream.flush();
            return arrayStream.toByteArray();
        }
    }
}
