package com.staffs.api.identity.api;

import com.staffs.api.identity.application.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
public class IdentityController {
    private final UserService userService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        return new LoginResponse(userService.login(req.getEmail(), req.getPassword()));
    }
}

