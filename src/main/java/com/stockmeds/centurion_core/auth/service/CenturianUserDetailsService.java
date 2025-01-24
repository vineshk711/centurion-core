package com.stockmeds.centurion_core.auth.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

import static com.stockmeds.centurion_core.enums.ErrorCode.USER_NOT_FOUND;

@Service
public class CenturianUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {
        // ⚡ Replace this with actual user lookup from DB
        if (phoneNumber.equals("987654321")) {
            throw new UsernameNotFoundException(USER_NOT_FOUND.getMessage());
        }

        return new User(phoneNumber, "", Collections.emptyList()); // No password required for OTP-based login
    }
}
