package com.han.pwac.pinguins.backend.repository.contract;

import com.han.pwac.pinguins.backend.domain.DTO.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IProjectDao extends IBaseDao<ProjectDto> {
    Collection<ProjectDto> getAllByBusinessId(int businessId);

    int storeProject(CreateProjectDto project, int supervisorId);

    Collection<ProjectDto> getBySupervisorId(int userId);

    Optional<Boolean> checkIfProjectNameIsTaken(String title, int supervisorId);

    List<ProjectDto> getByBusinessId(int businessId);
}