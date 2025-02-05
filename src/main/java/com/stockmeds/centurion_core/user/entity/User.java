package com.stockmeds.centurion_core.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stockmeds.centurion_core.account.entity.Account;
import com.stockmeds.centurion_core.user.dto.UserDTO;
import com.stockmeds.centurion_core.user.enums.UserRole;
import com.stockmeds.centurion_core.user.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "phone_number", unique = true, nullable = false)
    private String phoneNumber;

    @Column(name = "full_name")
    private String fullName;

    @Column(unique = true)
    private String email;

    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.OWNER;

    @Column(name = "is_verified")
    private boolean isVerified = false;

    @Column(name = "user_status")
    @Enumerated(EnumType.STRING)
    private UserStatus userStatus = UserStatus.ACTIVE;

    @Column(name = "account_id")
    private Integer accountId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    private Account account;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UserDTO toUserDTO() {
        return UserDTO.builder()
                .id(this.getId())
                .phoneNumber(this.getPhoneNumber())
                .fullName(this.getFullName())
                .email(this.getEmail())
                .role(this.getRole())
                .userStatus(this.getUserStatus())
                .isVerified(this.isVerified())
                .accountId(this.getAccountId())
                .build();
    }
}

