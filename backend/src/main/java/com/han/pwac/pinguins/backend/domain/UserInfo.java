package com.han.pwac.pinguins.backend.domain;


import com.han.pwac.pinguins.backend.authentication.Provider;

public record UserInfo(
        Provider provider,
        String id,
        String name,
        String avatarUrl,
        String email
) {
}
