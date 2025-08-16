package com.stockmeds.centurion_core.user.service;

import com.stockmeds.centurion_core.account.record.Account;
import com.stockmeds.centurion_core.account.service.AccountService;
import com.stockmeds.centurion_core.auth.record.UserAccountAttributes;
import com.stockmeds.centurion_core.config.CenturionThreadLocal;
import com.stockmeds.centurion_core.exception.CustomException;
import com.stockmeds.centurion_core.user.record.User;
import com.stockmeds.centurion_core.user.entity.UserEntity;
import com.stockmeds.centurion_core.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.stockmeds.centurion_core.enums.ErrorCode.USER_NOT_FOUND;
import static io.micrometer.common.util.StringUtils.isNotBlank;
import static java.util.Objects.nonNull;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final AccountService accountService;

    public UserService(UserRepository userRepository,
                       AccountService accountService) {
        this.userRepository = userRepository;
        this.accountService = accountService;
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
                    UserEntity savedUser = userRepository.save(userEntity);
                    accountService.createAccount(savedUser.getId());
                    return User.fromUserEntity(savedUser);
                });
    }



    public User updateUser(User user) {
        Integer userId = Optional.ofNullable(user.id())
                .orElseGet(() -> CenturionThreadLocal.getUserAccountAttributes().getUserId());
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));

        if (isNotBlank(user.fullName())) userEntity.setFullName(user.fullName());
        if (isNotBlank(user.email())) userEntity.setEmail(user.email());
        if (nonNull(user.role())) userEntity.setRole(user.role());
        if (nonNull(user.userStatus())) userEntity.setUserStatus(user.userStatus());
        if (nonNull(user.isVerified())) userEntity.setVerified(user.isVerified());

        return User.fromUserEntity(userRepository.save(userEntity));
    }
}
