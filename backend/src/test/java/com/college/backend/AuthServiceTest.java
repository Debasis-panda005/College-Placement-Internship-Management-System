
package com.college.backend;

import com.college.backend.entity.User;
import com.college.backend.repository.UserRepository;
import com.college.backend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository);
    }

    @Test
    void registerShouldHashPasswordAndAssignStudentRole() {
        User input = new User(
                "Test Student",
                "Student@Example.com",
                "StrongPass123",
                "ADMIN"
        );

        when(userRepository.existsByEmail("student@example.com"))
                .thenReturn(false);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User saved = invocation.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });

        User result = authService.register(input);

        assertEquals("Test Student", result.getName());
        assertEquals("student@example.com", result.getEmail());
        assertEquals("STUDENT", result.getRole());
        assertNotEquals("StrongPass123", result.getPassword());

        verify(userRepository).save(input);
    }

    @Test
    void registerShouldRejectDuplicateEmail() {
        User input = new User(
                "Test Student",
                "student@example.com",
                "StrongPass123",
                "STUDENT"
        );

        when(userRepository.existsByEmail("student@example.com"))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(input)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerShouldRejectShortPassword() {
        User input = new User(
                "Test Student",
                "student@example.com",
                "short",
                "STUDENT"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(input)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginShouldRejectIncorrectPassword() {
        User storedUser = new User(
                "Test Student",
                "student@example.com",
                "$2a$10$invalidExampleHash",
                "STUDENT"
        );

        when(userRepository.findByEmail("student@example.com"))
                .thenReturn(Optional.of(storedUser));

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(
                        "student@example.com",
                        "WrongPassword123"
                )
        );
    }

    @Test
    void registerShouldRejectMissingName() {
        User input = new User(
                " ",
                "student@example.com",
                "StrongPass123",
                "STUDENT"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(input)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginShouldRejectBlankCredentials() {
        assertThrows(
                IllegalArgumentException.class,
                () -> authService.login("", "")
        );

        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void loginShouldSucceedWithCorrectPassword() {
        String rawPassword = "StrongPass123";
        String hashedPassword = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                .encode(rawPassword);

        User storedUser = new User(
                "Test Student",
                "student@example.com",
                hashedPassword,
                "STUDENT"
        );

        when(userRepository.findByEmail("student@example.com"))
                .thenReturn(Optional.of(storedUser));

        User result = authService.login(
                "student@example.com",
                rawPassword
        );

        assertEquals("student@example.com", result.getEmail());
        assertEquals("STUDENT", result.getRole());
    }

    @Test
    void loginShouldRejectUnknownEmail() {
        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(
                        "unknown@example.com",
                        "StrongPass123"
                )
        );
    }
}