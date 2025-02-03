package com.stockmeds.centurion_core.auth.service.impl;

import com.stockmeds.centurion_core.auth.service.SmsService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TwilioSmsService implements SmsService {

    public static final String ACCOUNT_SID = "ACe62b7b00e0b775981e9cc726ec5bca19";
    public static final String AUTH_TOKEN = "xxxxxxxxx";
    public static final String FROM = "+645665323";


    @Async
    @Override
    public void sendSms(String phoneNumber, String otp) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        Message message = Message
                .creator(
                        new PhoneNumber(phoneNumber),
                        new PhoneNumber(FROM),
                        String.format("%s is your StockMeds one time password. This OTP valid for 5 minutes", otp)
                )
                .create();
        log.info("Sending otp sms to phone: {}, sid: {}", phoneNumber, message.getSid());
    }
}
