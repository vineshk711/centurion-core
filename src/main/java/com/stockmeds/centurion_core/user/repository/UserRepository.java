package com.stockmeds.centurion_core.user.repository;

import com.stockmeds.centurion_core.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByPhoneNumber(String phoneNumber);
    List<User> findByAccountId(Integer accountId);

    @Query(value = """
        INSERT INTO users (phone_number) 
        VALUES (:phoneNumber) 
        ON CONFLICT (phone_number) DO NOTHING 
        RETURNING *;
        """, nativeQuery = true)
    Optional<User> insertIfAbsent(String phoneNumber);
}
