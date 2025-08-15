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
    AccountStatus accountStatus,
    String imageUrl
) {
    public AccountEntity toAccountEntity() {
        return AccountEntity.builder()
                .id(this.id())
                .name(this.name())
                .ownerId(this.ownerId())
                .address(this.address())
                .gstNumber(this.gstNumber())
                .drugLicenseNumber(this.drugLicenseNumber())
                .accountStatus(this.accountStatus())
                .imageUrl(this.imageUrl())
                .build();
    }
}
