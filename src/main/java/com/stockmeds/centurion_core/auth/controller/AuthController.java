package com.stockmeds.centurion_core.auth.controller;

import com.stockmeds.centurion_core.auth.record.OtpRequest;
import com.stockmeds.centurion_core.auth.record.OtpResponse;
import com.stockmeds.centurion_core.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<OtpResponse> sendOtp(@RequestBody OtpRequest loginRequest) {
        return ResponseEntity.ok(authService.sendOtp(loginRequest));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<OtpResponse> verifyOtp(@RequestBody OtpRequest loginRequest) {
        return ResponseEntity.ok(authService.verifyOtp(loginRequest));
    }
}
