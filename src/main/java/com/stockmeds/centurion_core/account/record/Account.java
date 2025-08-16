package com.stockmeds.centurion_core.account.record;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.stockmeds.centurion_core.account.entity.AccountEntity;
import com.stockmeds.centurion_core.account.enums.AccountStatus;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public record Account(
    Integer id,
    String name,
    Integer ownerId,
    String address,
    String gstNumber,
    String drugLicenseNumber,
    AccountStatus accountStatus
) {

    public static Account fromAccountEntity(AccountEntity accountEntity) {
        return new Account(
            accountEntity.getId(),
            accountEntity.getName(),
            accountEntity.getOwnerId(),
            accountEntity.getAddress(),
            accountEntity.getGstNumber(),
            accountEntity.getDrugLicenseNumber(),
            accountEntity.getAccountStatus()
        );
    }
}
