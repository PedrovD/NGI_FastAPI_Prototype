package com.han.pwac.pinguins.backend.domain.DTO;

import jakarta.validation.Valid;
import org.hibernate.validator.constraints.Length;

public record GetSkillWithDescriptionDto(
        GetSkillDto skill,
        @Length(max = 400)
        String description
) {
}
