package com.han.pwac.pinguins.backend.domain.DTO;

public record BusinessDto(
        int businessId,
        String name,
        String description,
        FileDto photo,
        String location
) implements IValidate {
    public static final int NAME_LENGTH = 255;
    public static final int DESCRIPTION_LENGTH = 4000;
    public static final int LOCATION_LENGTH = 255;

    @Override
    public boolean isValid() {
        return name != null && name.length() <= NAME_LENGTH &&
                description != null && IValidate.stripMarkdownFromString(description).length() <= DESCRIPTION_LENGTH &&
                location != null && location.length() <= LOCATION_LENGTH &&
                photo != null && photo.isValid();
    }
}
