package com.han.pwac.pinguins.backend.services.contract;

import com.han.pwac.pinguins.backend.domain.DTO.BusinessProjectsWithTasksAndSkillsDto;
import com.han.pwac.pinguins.backend.domain.DTO.CreateProjectDto;
import com.han.pwac.pinguins.backend.domain.DTO.GetProjectDto;
import com.han.pwac.pinguins.backend.domain.DTO.ProjectDto;

import java.util.Collection;
import java.util.List;

public interface IProjectService extends IBaseService<ProjectDto> {

    Collection<GetProjectDto> getAllProjectsWithSkills();

    Collection<GetProjectDto> getAllByBusinessId(int businessId);

    int createProject(CreateProjectDto project, int supervisorId);

    GetProjectDto getProject(int projectId);

    Collection<BusinessProjectsWithTasksAndSkillsDto> getAllBusinessesWithProjectsAndTasks();
}
