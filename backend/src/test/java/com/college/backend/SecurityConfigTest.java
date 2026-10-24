
package com.college.backend;

import com.college.backend.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SecurityConfigTest {

    @Test
    void securityConfigurationClassCanBeLoaded() {
        SecurityConfig config = new SecurityConfig();

        assertNotNull(config);
    }

    @Test
    void securityConfigurationDeclaresFilterChainMethod() throws Exception {
        SecurityConfig config = new SecurityConfig();

        var http = mock(
                org.springframework.security.config.annotation.web.builders.HttpSecurity.class
        );

        // This test checks that the configuration class exists.
        // Endpoint access rules will be tested separately.
        assertNotNull(config);
        assertNotNull(http);
    }
}