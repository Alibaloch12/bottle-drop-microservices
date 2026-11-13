package de.fhdortmund.bottledrop.usermanagementservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security Configuration for the User Management Service.
 * This class configures global security beans.
 * We use BCrypt for hashing user passwords before storing them in the database
 * to ensure high security and prevent plain-text password leaks.
 */
@Configuration
public class SecurityConfig {

    /**
     * Provides a BCryptPasswordEncoder bean to the Spring Context.
     * This allows us to inject PasswordEncoder into our UserService.
     * @return A BCryptPasswordEncoder instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
