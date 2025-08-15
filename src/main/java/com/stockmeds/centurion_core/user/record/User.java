package com.stockmeds.centurion_core.user.record;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.stockmeds.centurion_core.user.entity.UserEntity;
import com.stockmeds.centurion_core.user.enums.UserRole;
import com.stockmeds.centurion_core.user.enums.UserStatus;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public record User(
    Integer id,
    String phoneNumber,
    String fullName,
    String email,
    UserRole role,
    UserStatus userStatus,
    Boolean isVerified,
    Integer accountId
) {

    public static User fromUserEntity(UserEntity userEntity) {
        return new User(
                userEntity.getId(),
                userEntity.getPhoneNumber(),
                userEntity.getFullName(),
                userEntity.getEmail(),
                userEntity.getRole(),
                userEntity.getUserStatus(),
                userEntity.isVerified(),
                userEntity.getAccountId()
        );
    }
}
