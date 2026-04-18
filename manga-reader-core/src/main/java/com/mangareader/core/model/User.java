package com.mangareader.core.model;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String username;
    private String passwordHash; // null for local single user
    private LocalDateTime createdAt;

    public User() {}

    public User(int id, String username) {
        this.id = id;
        this.username = username;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() { return "User{id=" + id + ", username='" + username + "'}"; }
}
