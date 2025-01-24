package com.stockmeds.centurion_core.auth.service;

import com.stockmeds.centurion_core.auth.dto.OtpResponse;
import com.stockmeds.centurion_core.exception.CustomException;
import com.stockmeds.centurion_core.utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import static com.stockmeds.centurion_core.enums.ErrorCode.INVALID_OTP;

@Service
public class AuthService {

    private final JwtUtil jwtUtil;
    private final OtpService otpService;
    private final SmsService smsService;

    public AuthService(
            JwtUtil jwtUtil,
            OtpService otpService,
            SmsService smsService
    ) {
        this.jwtUtil = jwtUtil;
        this.otpService = otpService;
        this.smsService = smsService;
    }



    public OtpResponse sendOtp(String phoneNumber) {
        String otp = otpService.generateOtp(phoneNumber);
        smsService.sendSms(phoneNumber, otp);
        return OtpResponse.builder().message("OTP send successfully!").build();
    }

    public Object verifyOtp(String phoneNumber, String otp) {
        boolean isValid = otpService.validateOtp(phoneNumber, otp);
        if (isValid) {
            String jwtToken = jwtUtil.generateToken(phoneNumber);
            return OtpResponse.builder()
                    .token(jwtToken)
                    .message("success")
                    .build();
        }
        throw new CustomException(HttpStatus.UNAUTHORIZED, INVALID_OTP);
    }
}
