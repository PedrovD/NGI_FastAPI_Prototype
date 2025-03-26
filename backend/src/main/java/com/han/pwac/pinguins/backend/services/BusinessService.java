package com.han.pwac.pinguins.backend.services;

import com.han.pwac.pinguins.backend.domain.DTO.BusinessDto;
import com.han.pwac.pinguins.backend.domain.DTO.SupervisorDto;
import com.han.pwac.pinguins.backend.repository.BusinessDao;
import com.han.pwac.pinguins.backend.repository.SupervisorDao;
import org.springframework.beans.factory.annotation.Autowired;
import com.han.pwac.pinguins.backend.repository.contract.IBaseDao;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class BusinessService extends BaseService<BusinessDto> {
    private final SupervisorDao supervisorDao;

    @Autowired
    public BusinessService(IBaseDao<BusinessDto> dao, SupervisorDao supervisorDao) {
        super(dao);
        this.supervisorDao = supervisorDao;
    }

    public void addUserToBusiness(Integer businessId, Integer userId) {
        ((BusinessDao) dao).addUserToBusiness(businessId, userId);
    }
    
    public Collection<SupervisorDto> getAllSupervisors(Integer businessId) {
        return supervisorDao.getByBusinessId(businessId);
    }

    public String[] getAllEmails(Integer businessId) {
        return supervisorDao.getSupervisorEmails(businessId);
    }
}