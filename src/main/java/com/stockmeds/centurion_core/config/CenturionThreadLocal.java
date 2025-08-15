package com.stockmeds.centurion_core.config;

import com.stockmeds.centurion_core.auth.record.UserAccountAttributes;
import com.stockmeds.centurion_core.enums.ErrorCode;
import com.stockmeds.centurion_core.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static com.stockmeds.centurion_core.constants.Constants.USER_ACCOUNT_ATTRIBUTES;

@Slf4j
public final class CenturionThreadLocal {

    private CenturionThreadLocal() {}

    private static final InheritableThreadLocal<ConcurrentHashMap<String, Serializable>> THREAD_LOCAL =
            new InheritableThreadLocal<>() {
                @Override
                protected ConcurrentHashMap<String, Serializable> initialValue() {
                    return new ConcurrentHashMap<>();
                }
            };

    public static void put(String key, Serializable value) {
        Objects.requireNonNull(key, "Thread local key cannot be null");
        if (value != null) {
            THREAD_LOCAL.get().put(key, value);
        }
    }

    public static Serializable get(String key) {
        return THREAD_LOCAL.get().get(key);
    }

    public static void clear() {
        THREAD_LOCAL.get().clear();
    }

    public static void clearKey(String key) {
        Objects.requireNonNull(key, "Thread local key cannot be null");
        THREAD_LOCAL.get().remove(key);
    }

    public static UserAccountAttributes getUserAccountAttributes() {
        UserAccountAttributes userAccountAttributes = (UserAccountAttributes) THREAD_LOCAL.get().get(USER_ACCOUNT_ATTRIBUTES);
        if(Objects.isNull(userAccountAttributes)) {
            log.error("UserAccountAttributes not found");
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_REQUEST);
        }
        return userAccountAttributes;
    }
}
