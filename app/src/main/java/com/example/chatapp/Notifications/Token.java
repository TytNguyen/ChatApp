package com.example.chatapp.Notifications;

public class Token {
    private String token;

    public Token(String token) {
        this.token = token;
    }

    private Token() { }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
