package com.han.pwac.pinguins.backend.services;

import com.han.pwac.pinguins.backend.domain.DTO.BusinessDto;
import com.han.pwac.pinguins.backend.domain.InviteKey;
import com.han.pwac.pinguins.backend.exceptions.InvalidDataException;
import com.han.pwac.pinguins.backend.exceptions.NotFoundException;
import com.han.pwac.pinguins.backend.repository.InviteKeyDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InviteKeyServiceTest {

    @InjectMocks
    private InviteKeyService sut;

    @Mock
    private InviteKeyDao inviteKeyDao;

    @Mock BusinessService businessService;

    @Test
    public void saveKey_successfulEncryptionAndStorage() throws NoSuchAlgorithmException {
        // Arrange
        String plainKey = "test-plain-key";
        Integer businessId = 1;
        BusinessDto mockBusiness = new BusinessDto(1,"mockBusiness", "mockDescription",null,"mockImageUrl");
        String encryptedKey = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(plainKey.getBytes())).replace("+", "-");
        when(businessService.findById(businessId)).thenReturn(Optional.of(mockBusiness));
        // Act
        InviteKey result = sut.saveKey(plainKey, businessId);

        // Assert
        ArgumentCaptor<InviteKey> captor = ArgumentCaptor.forClass(InviteKey.class);
        verify(inviteKeyDao, times(1)).add(captor.capture());
        InviteKey capturedKey = captor.getValue();

        assertEquals(encryptedKey, capturedKey.getKey());
        assertEquals(businessId, capturedKey.getBusinessId());
        assertNotNull(capturedKey.getDateTime());

        assertEquals(encryptedKey, result.getKey());
    }

    @Test
    public void saveKey_throwsNotFoundExceptionForInvalidBusinessId() {
        // Arrange
        String plainKey = "test-plain-key";

        // Act
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            sut.saveKey(plainKey, 1);
        });
    }

    @Test
    public void encryptKey_successfulEncryption() throws NoSuchAlgorithmException {
        // Arrange
        String plainKey = "test-plain-key";
        String expectedEncryptedKey = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(plainKey.getBytes()));

        // Act
        String result = sut.encryptKey(plainKey);

        // Assert
        assertEquals(expectedEncryptedKey, result);
    }

    @Test
    public void encryptKey_throwsInvalidDataException() throws NoSuchAlgorithmException {
        // Arrange
        InviteKeyService serviceWithInvalidAlgorithm = new InviteKeyService(inviteKeyDao, businessService) {
            @Override
            public String encryptKey(String plainKey) {
                throw new InvalidDataException("Error encrypting key: Invalid algorithm");
            }
        };

        // Act & Assert
        InvalidDataException exception = assertThrows(InvalidDataException.class, () -> {
            serviceWithInvalidAlgorithm.encryptKey("test-plain-key");
        });
        assertTrue(exception.getMessage().contains("Error encrypting key"));
    }

    @Test
    public void encryptKey_throwsRuntimeExceptionForInvalidAlgorithm() {
        // Arrange
        InviteKeyService serviceWithInvalidAlgorithm = new InviteKeyService(inviteKeyDao, businessService) {
            @Override
            public String encryptKey(String plainKey) {
                throw new RuntimeException("Error encrypting key: Invalid algorithm");
            }
        };

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            serviceWithInvalidAlgorithm.encryptKey("test-plain-key");
        });
        assertTrue(exception.getMessage().contains("Error encrypting key"));
    }

    @Test
    public void getInviteKeyFromCode_successfulRetrieval() {
        // Arrange
        String code = "test-code";
        InviteKey mockInviteKey = new InviteKey(code, 1, new Timestamp(System.currentTimeMillis()));
        when(inviteKeyDao.findByKey(code)).thenReturn(Optional.of(mockInviteKey));

        // Act
        InviteKey result = sut.getInviteKeyFromCode(code);

        // Assert
        assertEquals(mockInviteKey, result);
    }

    @Test
    public void getInviteKeyFromCode_throwsNotFoundExceptionForExpiredKey() {
        // Arrange
        String code = "test-code";
        InviteKey mockInviteKey = new InviteKey(code, 1, Timestamp.valueOf("2021-01-01 00:00:00"));
        when(inviteKeyDao.findByKey(code)).thenReturn(Optional.of(mockInviteKey));

        // Act
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            sut.getInviteKeyFromCode(code);
        });
    }

    @Test
    public void getInviteKeyFromCode_throwsNotFoundException() {
        // Arrange
        String code = "test-code";
        InviteKey mockInviteKey = new InviteKey(code, 1, Timestamp.valueOf("2021-01-01 00:00:00"));
        when(inviteKeyDao.findByKey(code)).thenReturn(Optional.empty());

        // Act
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            sut.getInviteKeyFromCode(code);
        });
    }

    @Test
    public void deleteKey_successfulDeletion() {
        // Arrange
        String code = "test-code";

        // Act
        sut.deleteKey(code);

        // Assert
        verify(inviteKeyDao, times(1)).delete(code);
    }
}