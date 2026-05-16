package com.observai.gateway.config;

import com.observai.common.security.JwtTokenService;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {
    @Bean
    JwtTokenService jwtTokenService(
            @Value("${observai.jwt.secret:observai-default-development-secret-key}") String secret,
            @Value("${observai.jwt.ttl-seconds:7200}") long ttlSeconds) {
        return new JwtTokenService(secret, Duration.ofSeconds(ttlSeconds));
    }
}

