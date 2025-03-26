package com.han.pwac.pinguins.backend.services;

import com.han.pwac.pinguins.backend.domain.User;
import com.han.pwac.pinguins.backend.exceptions.NotFoundException;
import com.han.pwac.pinguins.backend.repository.UserDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

class UserServiceTest {
    @Mock
    private UserDao userDAO;

    @InjectMocks
    private UserService sut;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void findById_happy() {
        // Arrange
        Integer id = 1;
        Optional<User> mockUser = Optional.of(new User(id, "testUser", "testPath", "test@email.com"));
        when(userDAO.findById(id)).thenReturn(mockUser);

        // Act
        Optional<User> result = sut.findById(id);

        // Assert
        assertEquals(mockUser, result, "Expected user should be returned");
        verify(userDAO, times(1)).findById(id);
    }

    @Test
    public void findByProviderId_happy() {
        // Arrange
        String providerId = "testProviderId";
        Optional<User> mockUser = Optional.of(new User(1, "testUser", "testPath", "test@email.com"));
        when(userDAO.getByProviderId(providerId)).thenReturn(mockUser);

        // Act
        Optional<User> result = sut.findByProviderId(providerId);

        // Assert
        assertEquals(mockUser, result, "Expected user should be returned");
        verify(userDAO, times(1)).getByProviderId(providerId);
    }
}