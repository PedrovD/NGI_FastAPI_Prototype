package com.han.pwac.pinguins.backend.domain.DTO;

import java.util.Optional;

public record RegistrationDto(
        int supervisorId,
        int taskId,
        int userId,
        String description,
        Optional<Boolean> accepted,
        String response
) implements IValidate {
    public static final int DESCRIPTION_LENGTH = 4000;
    public static final int RESPONSE_LENGTH = 400;

    @Override
    public boolean isValid() {
        // null check for accepted is just in case someone passes a null value instead of an optional
        return description != null && IValidate.stripMarkdownFromString(description).length() <= DESCRIPTION_LENGTH && accepted != null && response != null && response.length() <= RESPONSE_LENGTH;
    }
}
