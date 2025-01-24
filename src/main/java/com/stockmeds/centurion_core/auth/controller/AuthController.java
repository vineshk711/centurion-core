package com.stockmeds.centurion_core.auth.controller;

import com.stockmeds.centurion_core.auth.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<Object> sendOtp(@RequestParam String phoneNumber) {
        return ResponseEntity.ok(authService.sendOtp(phoneNumber));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Object> verifyOtp(@RequestParam String phoneNumber, @RequestParam String otp) {
        return ResponseEntity.ok(authService.verifyOtp(phoneNumber, otp));
    }
}
