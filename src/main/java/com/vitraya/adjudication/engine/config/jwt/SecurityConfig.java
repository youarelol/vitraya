package com.vitraya.adjudication.engine.config.jwt;

import com.vitraya.adjudication.engine.dto.response.RestAPIResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthorizationFilter jwtAuthorizationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthorizationFilter jwtAuthorizationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthorizationFilter = jwtAuthorizationFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Permit Auth related endpoints
                        .requestMatchers("/api/v1/auth/login", "api/v1/notifications/subscribe").permitAll()
                        .requestMatchers("/api/v1/auth/validate/insurer/token").permitAll()
                        .requestMatchers("/api/v1/cron/**").permitAll()
                        .requestMatchers("/api/v1/auth/verify/otp").permitAll()
                        // Permit Swagger-related endpoints
                        .requestMatchers(
                                "/swagger-ui/**",  // Swagger UI resources
                                "/v3/api-docs/**"   // OpenAPI specification
                        ).permitAll()
                        .requestMatchers("/api/v1/external/**").permitAll()
                        .requestMatchers("/api/v1/bill").permitAll()
                        .requestMatchers("/api/v1/claim").permitAll()
                        .requestMatchers("/api/v1/healthCheck").permitAll()
                        .requestMatchers("/api/v1/reports/**").permitAll()
                        .requestMatchers("/api/v1/claim/demo").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorizedHandler())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint unauthorizedHandler() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(RestAPIResponse.buildFail(
                            HttpServletResponse.SC_UNAUTHORIZED,
                            "Unauthorized: You don't have permission to access this resource.",
                            "401",
                            "Unauthorized: You don't have permission to access this resource.")
                    .toString());
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(RestAPIResponse.buildFail(
                            HttpServletResponse.SC_FORBIDDEN,
                            "Unauthorized: You don't have permission to access this resource.",
                            "401",
                            "Unauthorized: You don't have permission to access this resource.")
                    .toString());
        };
    }
}
