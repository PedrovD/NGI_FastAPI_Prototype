package com.han.pwac.pinguins.backend.services;

import com.han.pwac.pinguins.backend.domain.InviteKey;
import com.han.pwac.pinguins.backend.exceptions.InvalidDataException;
import com.han.pwac.pinguins.backend.exceptions.NotFoundException;
import com.han.pwac.pinguins.backend.repository.InviteKeyDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class InviteKeyService {

    private final InviteKeyDao inviteKeyDao;
    private final BusinessService businessService;

    public InviteKeyService(InviteKeyDao inviteKeyDao, BusinessService businessService) {

        this.inviteKeyDao = inviteKeyDao;
        this.businessService = businessService;
    }

    public InviteKey saveKey(String plainKey, Integer businessId) {
        if(businessId!= null && businessService.findById(businessId).isEmpty()) {
            throw new NotFoundException("Bedrijf niet gevonden");
        }
        InviteKey inviteKey = new InviteKey(encryptKey(plainKey).replace("+", "-"), businessId, Timestamp.valueOf(LocalDateTime.now()));
        inviteKeyDao.add(inviteKey);
        return inviteKey;
    }

    public String encryptKey(String plainKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plainKey.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new InvalidDataException("Error encrypting key: " + e.getMessage());
        }
    }

    public InviteKey getInviteKeyFromCode(String code) {
        InviteKey inviteKey = inviteKeyDao.findByKey(code).orElseThrow(() -> new NotFoundException("Uitnodiging niet gevonden"));

        if (inviteKey.getDateTime().toLocalDateTime().plusDays(7).isBefore(LocalDateTime.now())) {
            throw new NotFoundException("Uitnodiging is verlopen");
        }

        return inviteKey;
    }

    public void deleteKey(String code) {
        inviteKeyDao.delete(code);
    }
}
