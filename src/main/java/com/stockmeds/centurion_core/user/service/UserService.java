package com.stockmeds.centurion_core.user.service;

import com.stockmeds.centurion_core.auth.record.UserAccountAttributes;
import com.stockmeds.centurion_core.config.CenturionThreadLocal;
import com.stockmeds.centurion_core.user.record.User;
import com.stockmeds.centurion_core.user.entity.UserEntity;
import com.stockmeds.centurion_core.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUser() {
        UserAccountAttributes userAccountAttributes = CenturionThreadLocal.getUserAccountAttributes();
        return userRepository.findById(userAccountAttributes.getUserId())
                .map(User::fromUserEntity)
                .orElse(null);
    }

    public User getUserByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .map(User::fromUserEntity)
                .orElse(null);
    }

    @Transactional
    public User getOrSaveUserIfAbsent(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .map(User::fromUserEntity)
                .orElseGet(() -> {
                    UserEntity userEntity = new UserEntity();
                    userEntity.setPhoneNumber(phoneNumber);
                    return User.fromUserEntity(userRepository.save(userEntity));
                });
    }

}
