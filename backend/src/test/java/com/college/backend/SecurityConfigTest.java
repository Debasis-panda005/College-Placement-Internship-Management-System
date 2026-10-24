
package com.college.backend;

import com.college.backend.config.SecurityConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SecurityConfigTest {

    @Test
    void securityConfigurationClassCanBeCreated() {
        SecurityConfig config = new SecurityConfig();

        assertNotNull(config);
    }

    @Test
    void securityConfigurationHasSecurityFilterChainMethod() throws Exception {
        var method = SecurityConfig.class.getMethod(
                "securityFilterChain",
                org.springframework.security.config.annotation.web.builders.HttpSecurity.class
        );

        assertNotNull(method);
    }
}