package com.stockmeds.centurion_core.auth.service.impl;

import com.stockmeds.centurion_core.auth.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MockSmsService implements SmsService {

    @Override
    public void sendSms(String phoneNumber, String otp) {
        log.info("Sending sms to phone: {} with otp: {}", phoneNumber, otp);
    }
}
