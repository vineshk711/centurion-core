package com.stockmeds.centurion_core.user;

import com.stockmeds.centurion_core.user.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class User {
    private String name;
    private String phoneNumber;
    private String otp;
    private boolean isVerified;


    public UserEntity toEntity() {
        return new UserEntity(null, this.name, this.phoneNumber, this.otp, this.isVerified);
    }
}
