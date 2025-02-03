package com.stockmeds.centurion_core.auth.service;

import com.stockmeds.centurion_core.auth.dto.OtpRequest;
import com.stockmeds.centurion_core.auth.dto.OtpResponse;
import com.stockmeds.centurion_core.auth.service.impl.MockSmsService;
import com.stockmeds.centurion_core.exception.CustomException;
import com.stockmeds.centurion_core.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Qualifier;
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
            MockSmsService smsService
    ) {
        this.jwtUtil = jwtUtil;
        this.otpService = otpService;
        this.smsService = smsService;
    }



    public OtpResponse sendOtp(OtpRequest loginRequest) {
        String otp = otpService.generateOtp(loginRequest.getPhoneNumber());
        smsService.sendSms(loginRequest.getPhoneNumber(), otp);
        return OtpResponse.builder().message("OTP send successfully!").build();
    }

    public OtpResponse verifyOtp(OtpRequest loginRequest) {
        boolean isValid = otpService.validateOtp(loginRequest.getPhoneNumber(), loginRequest.getOtp());
        if (isValid) {
            String jwtToken = jwtUtil.generateToken(loginRequest.getPhoneNumber());
            return OtpResponse.builder()
                    .token(jwtToken)
                    .message("success")
                    .build();
        }
        throw new CustomException(HttpStatus.UNAUTHORIZED, INVALID_OTP);
    }
}
