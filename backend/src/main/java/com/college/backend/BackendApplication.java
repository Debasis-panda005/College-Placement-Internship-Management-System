
package com.college.backend;

import com.college.backend.servlet.JobServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    public ServletRegistrationBean<JobServlet> jobServletRegistration() {
        return new ServletRegistrationBean<>(new JobServlet(), "/jobs", "/jobs/*");
    }
}
