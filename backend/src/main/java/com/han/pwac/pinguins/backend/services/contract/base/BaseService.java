package com.han.pwac.pinguins.backend.services.contract.base;

import com.han.pwac.pinguins.backend.domain.DTO.IValidate;
import com.han.pwac.pinguins.backend.repository.contract.base.IBaseDao;
import com.han.pwac.pinguins.backend.services.contract.base.IBaseService;

import java.util.Collection;
import java.util.Optional;

public class BaseService<T extends IValidate, TId> implements IBaseService<T, TId> {
    protected final IBaseDao<T, TId> dao;

    public BaseService(IBaseDao<T, TId> dao) {
        this.dao = dao;
    }

    @Override
    public Collection<T> getAll() {
        return dao.getAll();
    }

    @Override
    public Optional<T> findById(TId id) {
        return dao.findById(id);
    }

    @Override
    public boolean update(TId id, T item) {
        if (!item.isValid()) {
            return false;
        }
        return dao.update(id, item);
    }

    @Override
    public boolean delete(TId id) {
        return dao.delete(id);
    }

    @Override
    public boolean add(T item) {
        if (!item.isValid()) {
            return false;
        }
        return dao.add(item);
    }

    public TId getLastInsertedId() {
        return dao.getLastInsertedId();
    }
}
