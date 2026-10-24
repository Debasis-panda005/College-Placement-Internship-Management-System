
package com.college.backend;

import com.college.backend.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SecurityConfigTest {

    @Test
    void securityConfigurationClassCanBeCreated() {
        SecurityConfig config = new SecurityConfig();

        assertNotNull(config);
    }

    @Test
    void securityConfigurationDeclaresFilterChainMethod() throws Exception {
        Method method = SecurityConfig.class.getMethod(
                "securityFilterChain",
                HttpSecurity.class
        );

        assertEquals(SecurityFilterChain.class, method.getReturnType());
    }

    @Test
    void securityConfigurationUsesBeanAnnotation() throws Exception {
        Method method = SecurityConfig.class.getMethod(
                "securityFilterChain",
                HttpSecurity.class
        );

        assertNotNull(method.getAnnotation(org.springframework.context.annotation.Bean.class));
    }
}