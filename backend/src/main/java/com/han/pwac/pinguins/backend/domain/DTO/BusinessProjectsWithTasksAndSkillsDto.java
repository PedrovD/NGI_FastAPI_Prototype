package com.han.pwac.pinguins.backend.domain.DTO;

import java.util.Collection;
import java.util.List;

public class BusinessProjectsWithTasksAndSkillsDto {
    private BusinessDto business;
    private List<ProjectWithTasksAndSkillsDto> projects;
    private List<GetSkillDto> topSkills;

    public BusinessProjectsWithTasksAndSkillsDto(BusinessDto business, List<ProjectWithTasksAndSkillsDto> projects, List<GetSkillDto> topSkills) {
        this.business = business;
        this.projects = projects;
        this.topSkills = topSkills;
    }

    public BusinessProjectsWithTasksAndSkillsDto() {
    }

    public BusinessDto getBusiness() {
        return business;
    }

    public void setBusiness(BusinessDto business) {
        this.business = business;
    }

    public List<ProjectWithTasksAndSkillsDto> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectWithTasksAndSkillsDto> projects) {
        this.projects = projects;
    }

    public List<GetSkillDto> getTopSkills() {
        return topSkills;
    }

    public void setTopSkills(List<GetSkillDto> topSkills) {
        this.topSkills = topSkills;
    }
}
