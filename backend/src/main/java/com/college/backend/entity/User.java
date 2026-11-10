package com.college.backend.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing an authenticated user account.
 * Supports STUDENT, COORDINATOR, and ADMIN roles.
 * Never stores or exposes plaintext passwords.
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_COORDINATOR = "COORDINATOR";
    public static final String ROLE_ADMIN = "ADMIN";

    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_PENDING_APPROVAL = "PENDING_APPROVAL";
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_REJECTED = "REJECTED";

    private static final Set<String> SUPPORTED_ROLES;

    static {
        Set<String> roles = new HashSet<>();
        roles.add(ROLE_STUDENT);
        roles.add(ROLE_COORDINATOR);
        roles.add(ROLE_ADMIN);
        SUPPORTED_ROLES = Collections.unmodifiableSet(roles);
    }

    private Long id;
    private String name;
    private String email;
    private String passwordHash;
    private String role;
    private String status = STATUS_APPROVED;
    private LocalDateTime createdAt;

    public User() {
    }

    public User(String name, String email, String passwordHash, String role) {
        this.name = name != null ? name.trim() : null;
        this.email = normalizeEmail(email);
        this.passwordHash = passwordHash;
        setRole(role);
        this.status = STATUS_APPROVED;
        this.createdAt = LocalDateTime.now();
    }

    public User(String name, String email, String passwordHash, String role, String status) {
        this.name = name != null ? name.trim() : null;
        this.email = normalizeEmail(email);
        this.passwordHash = passwordHash;
        setRole(role);
        this.status = status != null ? status.trim().toUpperCase() : null;
        this.createdAt = LocalDateTime.now();
    }

    public User(Long id, String name, String email, String passwordHash, String role, LocalDateTime createdAt) {
        this.id = id;
        this.name = name != null ? name.trim() : null;
        this.email = normalizeEmail(email);
        this.passwordHash = passwordHash;
        setRole(role);
        this.status = STATUS_APPROVED;
        this.createdAt = createdAt;
    }

    public User(Long id, String name, String email, String passwordHash, String role, String status, LocalDateTime createdAt) {
        this.id = id;
        this.name = name != null ? name.trim() : null;
        this.email = normalizeEmail(email);
        this.passwordHash = passwordHash;
        setRole(role);
        this.status = status != null ? status.trim().toUpperCase() : null;
        this.createdAt = createdAt;
    }

    /**
     * Normalizes an email address by trimming whitespace and converting to lower case.
     *
     * @param email input email string
     * @return trimmed lower-case email, or null if input is null
     */
    public static String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    /**
     * Validates whether a given role string belongs to the supported roles.
     *
     * @param role role to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidRole(String role) {
        if (role == null) {
            return false;
        }
        return SUPPORTED_ROLES.contains(role.trim().toUpperCase());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = normalizeEmail(email);
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        if (role == null || !isValidRole(role)) {
            throw new IllegalArgumentException("Unsupported user role: " + role
                    + ". Supported roles: " + SUPPORTED_ROLES);
        }
        this.role = role.trim().toUpperCase();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status != null ? status.trim().toUpperCase() : null;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Safe string representation that intentionally omits the passwordHash to prevent credential leakage.
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
