package com.han.pwac.pinguins.backend.domain.DTO;

import java.util.Optional;

public record GetRegistrationDto(
        int taskId,
        String reason,
        Optional<Boolean> accepted,
        String response,
        StudentDto student
) {

}
