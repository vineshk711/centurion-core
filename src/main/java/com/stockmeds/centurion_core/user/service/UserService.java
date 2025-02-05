package com.stockmeds.centurion_core.user.service;

import com.stockmeds.centurion_core.auth.dto.UserAccountAttributes;
import com.stockmeds.centurion_core.config.CenturionThreadLocal;
import com.stockmeds.centurion_core.user.dto.UserDTO;
import com.stockmeds.centurion_core.user.entity.User;
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

    public Object getUser(Integer userId) {
        UserAccountAttributes userAccountAttributes = CenturionThreadLocal.getUserAccountAttributes();
        log.info("userAccountAttributes {}", userAccountAttributes);
        return userRepository.findById(userId).map(User::toUserDTO).orElse(null);
    }

    public UserDTO getUserByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .map(User::toUserDTO)
                .orElse(null);
    }

    @Transactional
    public UserDTO getOrSaveUserIfAbsent(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .map(User::toUserDTO)
                .orElseGet(() -> userRepository.insertIfAbsent(phoneNumber)
                        .orElseGet(() -> userRepository.findByPhoneNumber(phoneNumber)
                                .orElseThrow(() -> new IllegalStateException("User should exist but was not found"))
                        ).toUserDTO()
                );
    }

}
