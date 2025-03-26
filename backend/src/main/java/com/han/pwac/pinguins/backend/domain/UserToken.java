package com.han.pwac.pinguins.backend.domain;

public class UserToken {
    private int userId;
    private String sessionToken;
    private int expiresIn;

    public UserToken() {}

    public UserToken(int userId, String sessionToken, int expiresIn) {
        this.userId = userId;
        this.sessionToken = sessionToken;
        this.expiresIn = expiresIn;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }
}
