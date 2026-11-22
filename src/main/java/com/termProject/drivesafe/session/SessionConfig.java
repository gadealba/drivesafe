package com.termProject.drivesafe.session;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Lets Spring inject the singleton. Spring does not create a second SessionManager:
 * it is handed the one instance returned by getInstance().
 */
@Configuration
public class SessionConfig {

    @Bean
    SessionManager sessionManager() {
        return SessionManager.getInstance();
    }
}
