package com.stockmeds.centurion_core.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.stockmeds.centurion_core.user.entity.User;
import com.stockmeds.centurion_core.user.enums.UserRole;
import com.stockmeds.centurion_core.user.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class UserDTO {
    private Integer id;
    private String phoneNumber;
    private String fullName;
    private String email;
    private UserRole role;
    private UserStatus userStatus;
    private Boolean isVerified;
    private Integer accountId;


    public User toUserEntity() {
        return User.builder()
                .id(this.getId())
                .phoneNumber(this.getPhoneNumber())
                .fullName(this.getFullName())
                .email(this.getEmail())
                .role(this.getRole())
                .userStatus(this.getUserStatus())
                .isVerified(this.getIsVerified())
                .accountId(this.getAccountId())
                .build();
    }
}
