package com.han.pwac.pinguins.backend.domain.DTO;

public record StudentRepositoryDto(
        int userId,
        String providerId,
        String username,
        String description,
        FileDto profilePicture,
        FileDto cv,
        String email
) implements IValidate {
    public static final int USERNAME_LENGTH = 50;
    public static final int DESCRIPTION_LENGTH = 4000;

    @Override
    public boolean isValid() {
        return username != null && username.length() <= USERNAME_LENGTH &&
                description != null && IValidate.stripMarkdownFromString(description).length() <= DESCRIPTION_LENGTH &&
                profilePicture != null && profilePicture.isValid() &&
                cv != null && cv.isValid();
    }
}
