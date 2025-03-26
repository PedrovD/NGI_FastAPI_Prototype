package com.han.pwac.pinguins.backend.services;

import com.han.pwac.pinguins.backend.domain.User;
import com.han.pwac.pinguins.backend.exceptions.NotFoundException;
import com.han.pwac.pinguins.backend.repository.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserDao userDAO;

    @Autowired
    public UserService(UserDao userDAO) {
        this.userDAO = userDAO;
    }

    public Optional<User> findByProviderId(String providerId) {
        return userDAO.getByProviderId(providerId);
    }

    public Optional<User> findById(Integer id) { return userDAO.findById(id); }
}
