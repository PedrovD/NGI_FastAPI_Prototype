package com.han.pwac.pinguins.backend.domain.contract;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public interface UploadableFile {
    long getSize();

    boolean isEmpty();

    String getOriginalFilename();

    byte[] getBytes() throws IOException;
}
