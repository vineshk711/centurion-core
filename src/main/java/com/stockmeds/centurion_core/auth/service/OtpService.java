package com.stockmeds.centurion_core.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class OtpService {
    private final Random random = new Random();
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();

    public String generateOtp(String phoneNumber) {
        String otp = String.valueOf(random.nextInt(9000) + 1000);
        otpStorage.put(phoneNumber, otp);
        return otp;
    }

    public boolean validateOtp(String phoneNumber, String otp) {
        return otp.equals(otpStorage.get(phoneNumber));
    }
}
