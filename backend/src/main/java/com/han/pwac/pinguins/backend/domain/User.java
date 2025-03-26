package com.han.pwac.pinguins.backend.domain;

public class User {
    private int userId;
    private String providerId;
    private String username;
    private String imagePath;
    private String email;


    public User() {
    }
    public User(int id, String username, String imagePath, String email) {
        this.userId = id;
        this.username = username;
        this.imagePath = imagePath;
        this.email = email;
    }

    public int getId() {
        return userId;
    }

    public void setId(int id) {
        this.userId = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + userId +
                ", username='" + username + '\'' +
                '}';
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
