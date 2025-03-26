package com.han.pwac.pinguins.backend.domain.DTO;

import java.util.Collection;

public record GetProjectDto(
        int id,
        String title,
        String description,
        Collection<GetSkillDto> projectTopSkills,
        BusinessDto business,
        FileDto photo
) implements IValidate {

    public static final int TITLE_LENGTH = 50;
    public static final int DESCRIPTION_LENGTH = 255;

    @Override
    public boolean isValid() {
        return title !=  null && title.length() <= TITLE_LENGTH &&
                description != null && description.length() <= DESCRIPTION_LENGTH &&
                projectTopSkills != null && projectTopSkills.size() <= 5 &&
                business != null && business.isValid() &&
                photo != null && photo.isValid();
    }
}
