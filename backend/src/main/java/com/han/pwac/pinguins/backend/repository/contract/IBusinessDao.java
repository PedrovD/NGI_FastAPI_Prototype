package com.han.pwac.pinguins.backend.repository.contract;

import com.han.pwac.pinguins.backend.domain.DTO.BusinessDto;

import java.util.Optional;

public interface IBusinessDao extends IBaseDao<BusinessDto> {
    Optional<BusinessDto> getByProjectId(int projectId);
}
