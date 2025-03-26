package com.han.pwac.pinguins.backend.domain.DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;
import java.util.Optional;

public record FileDto(
        Optional<String> path
) implements IValidate {
    public static String getFileStoragePath() {
        return System.getProperty("user.dir") + "/src/main/resources/static/files";
    }

    @JsonIgnore
    @Override
    public boolean isValid() {
        if (path == null) {
            return false;
        }
        if (path.isEmpty()) {
            return true;
        }
        File file = new File(getFileStoragePath() + path.get());
        return file.exists() && !file.isDirectory();
    }
}
