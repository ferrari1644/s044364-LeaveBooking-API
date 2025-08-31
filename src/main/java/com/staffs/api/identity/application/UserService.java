package com.staffs.api.identity.application;

import com.staffs.api.common.security.JwtTokenUtil;
import com.staffs.api.identity.infrastructure.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class UserService {
    private final AppUserRepository repo;
    private final JwtTokenUtil jwt;

    public String login(String email, String password) {
        var user = repo.findByEmail(email).orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!BCrypt.checkpw(password, user.getPasswordHash())) throw new RuntimeException("Invalid credentials");
        return jwt.generate(user.getEmail(), user.getRole().name());
    }
}

