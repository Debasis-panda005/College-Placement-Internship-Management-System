package com.college.backend.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.regex.Pattern;

/**
 * Service utility for secure password hashing and verification using BCrypt.
 * Uses a cryptographically secure random salt per hash.
 * Never stores or transmits plaintext passwords.
 */
public final class PasswordHashService {

    // Standard BCrypt regex pattern matching $2a$, $2b$, $2y$, or $2x$, 2-digit work factor, and 53 Radix-64 characters
    public static final Pattern BCRYPT_PATTERN =
            Pattern.compile("^\\$2[abxy]?\\$\\d{1,2}\\$[./0-9A-Za-z]{53}$");

    // Default BCrypt strength (work factor 10)
    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordHashService() {
        // Prevent instantiation of utility class
    }

    /**
     * Validates whether a candidate string conforms to a valid BCrypt hash format.
     * Prevents plaintext passwords or malformed strings from being mistaken for hashes.
     *
     * @param hash the candidate hash string to inspect
     * @return true if string conforms to standard BCrypt format, false otherwise
     */
    public static boolean isBCryptHash(String hash) {
        if (hash == null || hash.trim().isEmpty()) {
            return false;
        }
        return BCRYPT_PATTERN.matcher(hash.trim()).matches();
    }

    /**
     * Hashes a plaintext password using BCrypt with an automatically generated, unique salt.
     *
     * @param plainPassword the plaintext password to hash
     * @return the BCrypt hash string
     * @throws IllegalArgumentException if plainPassword is null or empty
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
        return ENCODER.encode(plainPassword);
    }

    /**
     * Verifies a plaintext password against a stored BCrypt hash.
     *
     * @param plainPassword the plaintext candidate password
     * @param storedHash the previously hashed BCrypt string
     * @return true if the candidate password matches the hash, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || plainPassword.isEmpty() || storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        try {
            return ENCODER.matches(plainPassword, storedHash);
        } catch (IllegalArgumentException e) {
            // Invalid BCrypt hash format
            return false;
        }
    }
}
