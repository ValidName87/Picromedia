package com.picromedia.models;

public class User {
    @PrimaryKey
    public long id;
    public String username;
    public String passwordHash;
    public String email;

    public User(String username, String passwordHash, String email) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
    }
}
