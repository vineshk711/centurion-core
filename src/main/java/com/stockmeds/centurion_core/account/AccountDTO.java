package com.stockmeds.centurion_core.account;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.stockmeds.centurion_core.account.entity.Account;
import com.stockmeds.centurion_core.account.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class AccountDTO {
    private Integer id;
    private String name;
    private Integer ownerId;
    private String address;
    private String gstNumber;
    private String drugLicenseNumber;
    private AccountStatus accountStatus;
    private String imageUrl;

    public Account toAccountEntity() {
        return Account.builder()
                .id(this.getId())
                .name(this.getName())
                .ownerId(this.getOwnerId())
                .address(this.getAddress())
                .gstNumber(this.getGstNumber())
                .drugLicenseNumber(this.getDrugLicenseNumber())
                .accountStatus(this.getAccountStatus())
                .imageUrl(this.getImageUrl())
                .build();
    }
}
