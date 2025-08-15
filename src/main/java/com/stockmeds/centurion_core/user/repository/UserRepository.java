package com.stockmeds.centurion_core.user.repository;

import com.stockmeds.centurion_core.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByPhoneNumber(String phoneNumber);
    List<UserEntity> findByAccountId(Integer accountId);

    @Query(value = """
        INSERT INTO users (phone_number) 
        VALUES (:phoneNumber) 
        ON CONFLICT (phone_number) DO NOTHING 
        RETURNING *;
        """, nativeQuery = true)
    Optional<UserEntity> insertIfAbsent(String phoneNumber);
}
