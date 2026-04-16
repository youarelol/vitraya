package com.vitraya.adjudication.engine.config.jwt;

import com.vitraya.adjudication.engine.mysql.entity.Users;
import com.vitraya.adjudication.engine.mysql.repository.UserRepository;
import com.vitraya.adjudication.engine.service.CustomUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, CustomUserDetailsService userDetailsService,
                                   UserRepository userRepository) {
        this.jwtTokenProvider = tokenProvider;
        this.customUserDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        log.info("Url Called {}", request.getRequestURI());

        String token = jwtTokenProvider.getTokenFromRequest(request);

        // Special condition for the refresh token endpoint
        if (request.getRequestURI().contains("check/refresh-token")) {
            // The refresh token validation will be handled by the controller
            // We just need to let the request pass through
            log.info("Refresh token endpoint called, skipping token validation");
        } else if (token != null && jwtTokenProvider.validateToken(token)) {
            Users user = userRepository.findByToken(token);
            if (user != null) {
                String username = jwtTokenProvider.getUsernameFromToken(token);
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                throw new ExpiredJwtException(null, null, "JWT Token has expired");
            }
        } else {
            log.info("Username not received");
        }

        chain.doFilter(request, response);
    }
}
