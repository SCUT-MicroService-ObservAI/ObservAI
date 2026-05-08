package com.observai.user.service;

import com.observai.common.dto.LoginRequest;
import com.observai.common.dto.LoginResponse;
import com.observai.common.security.JwtTokenService;
import java.util.Map;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class AuthService {
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtTokenService jwtTokenService;
    private final Map<String, String> users;

    public AuthService(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
        this.users = Map.of("ops", passwordEncoder.encode("123456"));
    }

    public LoginResponse login(LoginRequest request) {
        String encodedPassword = users.get(request.username());
        if (encodedPassword == null || !passwordEncoder.matches(request.password(), encodedPassword)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }
        return new LoginResponse(jwtTokenService.createToken(request.username()), jwtTokenService.expiresInSeconds());
    }
}

