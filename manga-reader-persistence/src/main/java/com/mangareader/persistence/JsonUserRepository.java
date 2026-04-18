package com.mangareader.persistence;

import com.mangareader.core.model.User;
import com.mangareader.core.repository.UserRepository;

import java.util.Optional;

public class JsonUserRepository implements UserRepository {
    private final JsonDatabase db;

    public JsonUserRepository(JsonDatabase db) {
        this.db = db;
    }

    @Override
    public User save(User user) {
        if (user.getId() <= 0) {
            user.setId(db.getData().nextUserId++);
            db.getData().users.add(user);
        } else {
            for (int i = 0; i < db.getData().users.size(); i++) {
                if (db.getData().users.get(i).getId() == user.getId()) {
                    db.getData().users.set(i, user);
                    break;
                }
            }
        }
        db.save();
        return user;
    }

    @Override
    public Optional<User> findById(int id) {
        return db.getData().users.stream()
                .filter(u -> u.getId() == id)
                .findFirst();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return db.getData().users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }

    @Override
    public User getLocalUser() {
        return findById(1).orElseThrow(() -> new RuntimeException("Default local user not found in JSON database."));
    }
}
