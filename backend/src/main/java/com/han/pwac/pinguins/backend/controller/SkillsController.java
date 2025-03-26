package com.han.pwac.pinguins.backend.controller;

import com.han.pwac.pinguins.backend.domain.DTO.GetSkillDto;
import com.han.pwac.pinguins.backend.services.SkillService;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/skills")
public class SkillsController {

    private final SkillService skillService;

    public SkillsController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping
    public Collection<GetSkillDto> getAllSkills() {
        return skillService.getAllSkills();
    }

    @PostMapping
    public GetSkillDto createSkill(@Length(max = 50) @RequestBody String skillName) {
        return skillService.createSkill(skillName);
    }

    @PatchMapping("/{skillId}/name")
    public ResponseEntity<?> updateSkillName(@PathVariable int skillId, @Length(max = 50) @RequestBody String skillName) {
        skillService.updateSkillName(skillId, skillName);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{skillId}/acceptance")
    public ResponseEntity<?> updateSkillAcceptance(@PathVariable int skillId, @RequestBody boolean isAccepted) {
        skillService.updateSkillAcceptance(skillId, isAccepted);
        return ResponseEntity.ok().build();
    }
}
