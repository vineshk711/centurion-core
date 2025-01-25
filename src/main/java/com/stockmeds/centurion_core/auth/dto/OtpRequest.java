package com.stockmeds.centurion_core.auth.dto;


import lombok.Data;

@Data
public class OtpRequest {
    private String phoneNumber;
    private String otp;
}
