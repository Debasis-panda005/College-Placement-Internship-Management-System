
package com.college.backend;

import com.college.backend.config.SecurityConfig;
import com.college.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void registrationEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/api/auth/register"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void loginEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/api/auth/login"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void registrationCreatesStudentWithoutExposingPassword()
            throws Exception {

        String email = "test-" + System.nanoTime() + "@example.com";

        String requestBody = """
                {
                    "name": "Test Student",
                    "email": "%s",
                    "password": "Password123",
                    "role": "ADMIN"
                }
                """.formatted(email);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Student"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void registrationRejectsShortPassword() throws Exception {

        String email = "short-" + System.nanoTime() + "@example.com";

        String requestBody = """
                {
                    "name": "Test Student",
                    "email": "%s",
                    "password": "123",
                    "role": "STUDENT"
                }
                """.formatted(email);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void loginRejectsIncorrectPassword() throws Exception {

        String email = "login-" + System.nanoTime() + "@example.com";

        String requestBody = """
                {
                    "name": "Login Student",
                    "email": "%s",
                    "password": "CorrectPassword123",
                    "role": "STUDENT"
                }
                """.formatted(email);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                        .param("email", email)
                        .param("password", "WrongPassword123"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void otherEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/protected-test"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    void duplicateRegistrationReturnsBadRequest() throws Exception {
        String email = "duplicate-" + System.nanoTime() + "@example.com";

        String requestBody = """
                {
                    "name": "Test Student",
                    "email": "%s",
                    "password": "Password123",
                    "role": "STUDENT"
                }
                """.formatted(email);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void successfulLoginReturnsSafeUserDetails() throws Exception {
        String email = "success-" + System.nanoTime() + "@example.com";

        String requestBody = """
                {
                    "name": "Test Student",
                    "email": "%s",
                    "password": "Password123",
                    "role": "STUDENT"
                }
                """.formatted(email);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                        .param("email", email)
                        .param("password", "Password123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }
}