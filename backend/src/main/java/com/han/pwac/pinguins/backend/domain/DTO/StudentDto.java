package com.han.pwac.pinguins.backend.domain.DTO;

import java.util.Collection;

public record StudentDto(
        int userId,
        String username,
        String description,
        FileDto profilePicture,
        String email,
        FileDto cv,
        Collection<GetSkillWithDescriptionDto> skills
) {
}
