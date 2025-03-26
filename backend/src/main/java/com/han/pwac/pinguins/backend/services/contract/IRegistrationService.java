package com.han.pwac.pinguins.backend.services.contract;

import com.han.pwac.pinguins.backend.domain.DTO.RegistrationDto;
import com.han.pwac.pinguins.backend.domain.RegistrationId;
import com.han.pwac.pinguins.backend.domain.DTO.GetRegistrationDto;
import com.han.pwac.pinguins.backend.domain.StudentEmailSelection;
import com.han.pwac.pinguins.backend.services.contract.base.IBaseService;

import java.util.Collection;

public interface IRegistrationService extends IBaseService<RegistrationDto, RegistrationId> {
    Collection<GetRegistrationDto> getAllRegistrationsForTask(int taskId);

    boolean addRegistration(int taskId, int userId, String reason);

    Collection<Integer> getRegistrationsForUser(int userId);

    Collection<String> getEmailAddressesForRegistrations(int selection, int taskId);
}
