package com.stockmeds.centurion_core.auth.service;

import com.stockmeds.centurion_core.auth.dto.UserAccountAttributes;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class CenturianUserDetailsService implements UserDetailsService {

    @Override
    public UserAccountAttributes loadUserByUsername(String phoneNumber) {
        return new UserAccountAttributes(phoneNumber);
    }
}
