package com.han.pwac.pinguins.backend.domain.DTO;

public record ProjectDto(
        int id,
        String title,
        String description,
        FileDto photo
) implements IValidate {
    @Override
    public boolean isValid() {
        return title != null && title.length() <= 255 &&
                description != null && IValidate.stripMarkdownFromString(description).length() <= 4000 &&
                photo != null && photo.isValid();
    }
}
