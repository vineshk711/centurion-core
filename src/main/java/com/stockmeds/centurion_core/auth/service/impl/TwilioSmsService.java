package com.stockmeds.centurion_core.auth.service.impl;

import com.stockmeds.centurion_core.auth.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TwilioSmsService implements SmsService {

    @Async
    @Override
    public void sendSms(String phoneNumber, String otp) {
        log.info("Sending otp:[{}] to phone: {}", otp, phoneNumber);
    }
}
