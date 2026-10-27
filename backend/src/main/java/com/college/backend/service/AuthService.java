package com.college.backend.service;

import com.college.backend.dao.UserDAO;
import com.college.backend.entity.User;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public User register(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User details are required");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (user.getPassword() == null || user.getPassword().length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters");
        }

        String email = user.getEmail().trim().toLowerCase();

        if (userDAO.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        user.setName(user.getName().trim());
        user.setEmail(email);

        // Public registration must not grant an elevated role.
        user.setRole("STUDENT");

        // UserDAO.create() hashes the password before storing it.
        return userDAO.create(user);
    }

    public User login(String email, String password) {
        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Email and password are required");
        }

        User user = userDAO.findByEmail(email.trim().toLowerCase());

        if (user == null
                || user.getPassword() == null
                || !BCrypt.checkpw(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return user;
    }
}