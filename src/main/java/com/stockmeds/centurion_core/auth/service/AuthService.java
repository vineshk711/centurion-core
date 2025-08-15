package com.stockmeds.centurion_core.auth.service;

import com.stockmeds.centurion_core.auth.record.OtpRequest;
import com.stockmeds.centurion_core.auth.record.OtpResponse;
import com.stockmeds.centurion_core.auth.service.impl.MockSmsService;
import com.stockmeds.centurion_core.exception.CustomException;
import com.stockmeds.centurion_core.user.record.User;
import com.stockmeds.centurion_core.user.service.UserService;
import com.stockmeds.centurion_core.utils.JwtUtil;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.stockmeds.centurion_core.constants.Constants.*;
import static com.stockmeds.centurion_core.enums.ErrorCode.INVALID_OTP;
import static java.util.Objects.isNull;

@Slf4j
@Service
public class AuthService {

    private final JwtUtil jwtUtil;
    private final OtpService otpService;
    private final SmsService smsService;
    private final UserService userService;

    public AuthService(
            JwtUtil jwtUtil,
            OtpService otpService,
            MockSmsService smsService,
            UserService userService
    ) {
        this.jwtUtil = jwtUtil;
        this.otpService = otpService;
        this.smsService = smsService;
        this.userService = userService;
    }



    public OtpResponse sendOtp(OtpRequest loginRequest) {
        String otp = otpService.generateOtp(loginRequest.phoneNumber());
        smsService.sendSms(loginRequest.phoneNumber(), otp);
        return new OtpResponse("OTP send successfully!", null);
    }

    public OtpResponse verifyOtp(OtpRequest loginRequest) {
        boolean isValid = otpService.validateOtp(loginRequest.phoneNumber(), loginRequest.otp());
        if (isValid) {
            //get user in case of sign in and save in case of sign up
            User user = userService.getOrSaveUserIfAbsent(loginRequest.phoneNumber());
            String jwtToken = jwtUtil.generateToken(loginRequest.phoneNumber(), getJwtClaims(user));
            return new OtpResponse("success", jwtToken);
        }
        throw new CustomException(HttpStatus.UNAUTHORIZED, INVALID_OTP);
    }


    private Map<String, Object> getJwtClaims(User user) {
        if (isNull(user)) return Collections.emptyMap();

        var claims = new HashMap<String, Object>();
        Optional.ofNullable(user.id()).ifPresent(id -> claims.put(USER_ID, id));
        Optional.ofNullable(user.accountId()).ifPresent(accountId -> claims.put(ACCOUNT_ID, accountId));
        return claims;
    }
}
