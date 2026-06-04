package com.taskforge.ui.session;

import com.taskforge.ui.model.UserModel;

// JWT and current user live in memory — never written to disk
public class SessionManager {

    private static SessionManager instance;

    private String token;
    private UserModel currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setSession(String token, UserModel user) {
        this.token = token;
        this.currentUser = user;
    }

    public void clearSession() {
        this.token = null;
        this.currentUser = null;
    }

    public String getToken() { return token; }
    public UserModel getCurrentUser() { return currentUser; }
    public boolean isLoggedIn() { return token != null; }

    public boolean isKetua() {
        return currentUser != null && "KETUA".equals(currentUser.getRole());
    }
}
