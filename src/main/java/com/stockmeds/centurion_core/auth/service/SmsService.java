package com.stockmeds.centurion_core.auth.service;

public interface SmsService {
    void sendSms(String phoneNumber, String otp);
}
