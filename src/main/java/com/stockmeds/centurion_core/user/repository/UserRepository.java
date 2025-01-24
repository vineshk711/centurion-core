package com.stockmeds.centurion_core.user.repository;

import com.stockmeds.centurion_core.user.User;
import com.stockmeds.centurion_core.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String> {
    Optional<User> findByPhoneNumber(String phoneNumber);
}
