package com.han.pwac.pinguins.backend.domain.DTO;

import com.han.pwac.pinguins.backend.domain.VerificationType;

import java.util.Optional;

public class VerificationDto {
    private final VerificationType type;
    private Integer userId;
    private Integer businessId;

    public VerificationDto(VerificationType type, Integer userId, Integer businessId) {
        this.type = type;
        this.userId = userId;
        this.businessId = businessId;
    }

    public VerificationType getType() {
        return type;
    }

    public Optional<Integer> getUserId() {
        return Optional.ofNullable(userId);
    }

    public void setUserId(int token) {
        this.userId = token;
    }

    public Optional<Integer> getBusinessId() {
        return Optional.ofNullable(businessId);
    }

    public void setBusinessId(Integer businessId) {
        this.businessId = businessId;
    }
}