package com.mangareader.core.repository;

import com.mangareader.core.model.User;
import java.util.Optional;

/**
 * Contract for user persistence.
 * Designed for single-user now; ready for multi-user auth in the future.
 */
public interface UserRepository {
    User save(User user);
    Optional<User> findById(int id);
    Optional<User> findByUsername(String username);
    /** Returns the default local user (id=1). */
    User getLocalUser();
}
