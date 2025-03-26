package com.han.pwac.pinguins.backend.domain.DTO;

import java.util.Optional;

public class CreateProjectDto {
    private String title;
    private String description;
    private FileDto imagePath;

    public CreateProjectDto(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public Optional<String> getTitle() {
        return Optional.ofNullable(title);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FileDto getImagePath() {
        return imagePath;
    }

    public void setImagePath(FileDto imagePage) {
        this.imagePath = imagePage;
    }
}
