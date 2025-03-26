package com.han.pwac.pinguins.backend.controller;

import com.han.pwac.pinguins.backend.authentication.Provider;
import com.han.pwac.pinguins.backend.domain.DTO.LinkDto;
import com.han.pwac.pinguins.backend.domain.DTO.VerificationDto;
import com.han.pwac.pinguins.backend.domain.InviteKey;
import com.han.pwac.pinguins.backend.domain.UserInfo;
import com.han.pwac.pinguins.backend.domain.VerificationType;
import com.han.pwac.pinguins.backend.services.InviteKeyService;
import com.han.pwac.pinguins.backend.services.UserTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InviteControllerTest {
    @InjectMocks
    private InviteController sut;

    @Mock
    private InviteKeyService inviteKeyService;
    @Mock
    private UserTokenService userTokenService;

    @Test
    public void invite_ValidBusinessIdReturnsInviteLink() {
        // Arrange
        Integer businessId = 1;
        UserInfo userInfo = new UserInfo(Provider.GOOGLE, "ss", "ss", "ss", "ss");

        String mockKey = "mock-key";
        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
        InviteKey mockInviteKey = new InviteKey(mockKey, businessId, timestamp);
        when(inviteKeyService.saveKey(anyString(), eq(businessId))).thenReturn(mockInviteKey);
        when(userTokenService.getVerificationByProviderId(any())).thenReturn(new VerificationDto(VerificationType.SUPERVISOR, 1, 1));

        // Act
        LinkDto actualLink = sut.invite(businessId, userInfo);

        // Assert
        verify(inviteKeyService, times(1)).saveKey(anyString(), eq(businessId));
    }
    @Test
    public void invite_NoBusinessIdReturnsInviteLink() {
        // Arrange
        Integer businessId = null;
        UserInfo userInfo = new UserInfo(Provider.GOOGLE, "ss", "ss", "ss", "ss");

        String mockKey = "mock-key";
        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
        InviteKey mockInviteKey = new InviteKey(mockKey, businessId, timestamp);
        when(inviteKeyService.saveKey(anyString(), eq(businessId))).thenReturn(mockInviteKey);
        when(userTokenService.getVerificationByProviderId(any())).thenReturn(new VerificationDto(VerificationType.TEACHER, 1, 1));

        // Act
        LinkDto actualLink = sut.invite(businessId, userInfo);

        // Assert
        verify(inviteKeyService, times(1)).saveKey(anyString(), eq(businessId));
    }
}