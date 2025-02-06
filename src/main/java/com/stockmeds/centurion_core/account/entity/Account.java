package com.stockmeds.centurion_core.account.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.stockmeds.centurion_core.account.AccountDTO;
import com.stockmeds.centurion_core.account.enums.AccountStatus;
import com.stockmeds.centurion_core.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "accounts")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", unique = true, insertable = false, updatable = false)
    private User owner;

    @Column(name = "owner_id")
    private Integer ownerId;

    private String address;

    @Column(name = "gst_number", unique = true)
    private String gstNumber;

    @Column(name = "drug_license_number", unique = true)
    private String drugLicenseNumber;

    @Column(name = "account_status")
    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public AccountDTO toAccountDTO() {
        return AccountDTO.builder()
               .id(id)
               .name(name)
               .ownerId(ownerId)
               .address(address)
               .gstNumber(gstNumber)
               .drugLicenseNumber(drugLicenseNumber)
               .accountStatus(accountStatus)
               .imageUrl(imageUrl)
               .build();
    }
}

