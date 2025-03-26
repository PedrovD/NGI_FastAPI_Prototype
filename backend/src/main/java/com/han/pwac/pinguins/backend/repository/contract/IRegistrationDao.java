package com.han.pwac.pinguins.backend.repository.contract;

import com.han.pwac.pinguins.backend.domain.DTO.GetRegistrationDto;
import com.han.pwac.pinguins.backend.domain.DTO.RegistrationDto;
import com.han.pwac.pinguins.backend.domain.RegistrationId;

import java.sql.Timestamp;
import java.util.Collection;


public interface IRegistrationDao extends com.han.pwac.pinguins.backend.repository.contract.base.IBaseDao<RegistrationDto, RegistrationId> {
    Collection<Integer> getRegistrationsForUser(int userId);

    Collection<RegistrationDto> getRegistrationsAfter(Timestamp date);
}