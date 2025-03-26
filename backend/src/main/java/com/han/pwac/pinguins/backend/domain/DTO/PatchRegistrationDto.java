package com.han.pwac.pinguins.backend.domain.DTO;

import org.hibernate.validator.constraints.Length;

public record PatchRegistrationDto(
        int taskId,
        int userId,
        boolean accepted,
        @Length(max=RegistrationDto.RESPONSE_LENGTH)
        String response
) {
}
