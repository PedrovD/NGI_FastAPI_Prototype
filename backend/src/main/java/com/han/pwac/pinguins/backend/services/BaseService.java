package com.han.pwac.pinguins.backend.services;

import com.han.pwac.pinguins.backend.domain.DTO.IValidate;
import com.han.pwac.pinguins.backend.repository.contract.base.IBaseDao;
import com.han.pwac.pinguins.backend.services.contract.IBaseService;

import java.util.Collection;
import java.util.Optional;

public class BaseService<T extends IValidate> extends com.han.pwac.pinguins.backend.services.contract.base.BaseService<T, Integer> implements IBaseService<T> {
    public BaseService(IBaseDao<T, Integer> dao) {
        super(dao);
    }
}
