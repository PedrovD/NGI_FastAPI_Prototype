package com.han.pwac.pinguins.backend.controller;

import com.han.pwac.pinguins.backend.domain.DTO.GetSkillDto;
import com.han.pwac.pinguins.backend.services.SkillService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SkillsControllerTest {
    @InjectMocks
    private SkillsController sut;
    @Mock
    private SkillService skillService;

    @Test
    public void getAllSkills_successfulAttempt() {
        // Arrange
        Collection<GetSkillDto> skills = Arrays.asList(new GetSkillDto(1, "Skill1", false), new GetSkillDto(2, "Skill2", false));
        when(skillService.getAllSkills()).thenReturn(skills);

        // Act
        Collection<GetSkillDto> result = sut.getAllSkills();

        // Assert
        assertEquals(skills, result);
    }

    @Test
    public void createSkill_successfulAttempt() {
        // Arrange
        String skillName = "Skill1";
        GetSkillDto skill = new GetSkillDto(1, skillName, false);
        when(skillService.createSkill(skillName)).thenReturn(skill);

        // Act
        GetSkillDto result = sut.createSkill(skillName);

        // Assert
        assertEquals(skill, result);
    }

    @Test
    public void updateSkillName_successfulAttempt() {
        // Arrange
        int skillId = 1;
        String skillName = "Skill1";

        // Act
        sut.updateSkillName(skillId, skillName);

        // Assert
        verify(skillService, times(1)).updateSkillName(skillId, skillName);
    }

    @Test
    public void updateSkillAcceptance_successfulAttempt() {
        // Arrange
        int skillId = 1;
        boolean isAccepted = false;

        // Act
        sut.updateSkillAcceptance(skillId, isAccepted);

        // Assert
        verify(skillService, times(1)).updateSkillAcceptance(skillId, isAccepted);
    }
}
