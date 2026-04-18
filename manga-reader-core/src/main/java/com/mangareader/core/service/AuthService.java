package com.mangareader.core.service;

import com.mangareader.core.model.User;
import com.mangareader.core.repository.UserRepository;
import java.util.Optional;

/**
 * Authentication service.
 * Currently supports single local user only.
 * Designed to support full multi-user login in the future —
 * just implement the login(username, password) method.
 */
public class AuthService {

    private final UserRepository userRepository;
    private User currentUser;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Auto-login as the default local user (no password). */
    public User loginAsLocalUser() {
        currentUser = userRepository.getLocalUser();
        return currentUser;
    }

    /**
     * Future: authenticate with username and password hash.
     * Not implemented yet — throws UnsupportedOperationException.
     */
    public Optional<User> login(String username, String password) {
        throw new UnsupportedOperationException(
            "Multi-user authentication not yet implemented. Use loginAsLocalUser().");
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        if (currentUser == null) {
            throw new IllegalStateException("No user is logged in. Call loginAsLocalUser() first.");
        }
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
