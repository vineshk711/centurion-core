package com.stockmeds.centurion_core.config;

import com.stockmeds.centurion_core.auth.dto.UserAccountAttributes;
import com.stockmeds.centurion_core.auth.service.CenturianUserDetailsService;
import com.stockmeds.centurion_core.utils.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

import static com.stockmeds.centurion_core.constants.Constants.*;
import static com.stockmeds.centurion_core.enums.ErrorCode.INVALID_JWT;
import static com.stockmeds.centurion_core.enums.ErrorCode.JWT_EXPIRED;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CenturianUserDetailsService userDetailsService;

    JwtAuthenticationFilter(
            JwtUtil jwtUtil,
            CenturianUserDetailsService userDetailsService
    ) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }
        String token = authHeader.substring(7);

        try {
            Map<String, Object> jwtPayload = jwtUtil.getTokenPayload(token);
            UserAccountAttributes accountAttributes = userDetailsService.loadUserByUsername(jwtPayload.get(JWT_SUBJECT).toString());
            accountAttributes.setUserId((Integer) jwtPayload.get(USER_ID));
            accountAttributes.setAccountId((Integer) jwtPayload.get(ACCOUNT_ID));

            if (!jwtUtil.isTokenExpired(token)) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(accountAttributes, null, accountAttributes.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtException e) {
            log.warn("Unauthorized access, token expired: [{}] {}", request.getMethod(), request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(JWT_EXPIRED.getMessage());
            return;
        } catch (Exception e) {
            log.warn("Unauthorized access, invalid token: [{}] {}", request.getMethod(), request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(INVALID_JWT.getMessage());
            return;
        }

        chain.doFilter(request, response);
    }
}
