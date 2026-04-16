package com.vitraya.adjudication.engine.service;

import com.vitraya.adjudication.engine.mysql.entity.Users;
import com.vitraya.adjudication.engine.mysql.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        log.info("User found {} with username: {}", user, username);
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(UUID.randomUUID().toString()) // We have disabled password authentication. Only OTP is used.
                .roles("")
                .build();
    }
}
