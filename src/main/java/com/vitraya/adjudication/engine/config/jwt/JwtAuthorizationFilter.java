package com.vitraya.adjudication.engine.config.jwt;

import com.vitraya.adjudication.engine.service.CustomUserDetailsService;
import com.vitraya.adjudication.engine.service.UserAccessService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    private final UserAccessService userAccessService;

    public JwtAuthorizationFilter(JwtTokenProvider jwtTokenProvider, CustomUserDetailsService customUserDetailsService,
                                  UserAccessService userAccessService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
        this.userAccessService = userAccessService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = request.getHeader("Authorization");
        log.info("Session check for authToken: {}, platform: {}, uri: {}", token, "2.0", request.getRequestURI());
        // Get the token from the request body by name refreshToken
        if (StringUtils.hasText(request.getParameter("refreshToken"))) {
            token = request.getParameter("refreshToken");
        }

        // Special condition for the refresh token endpoint
        if (request.getRequestURI().contains("check/refresh-token") || request.getRequestURI().contains("logout")) {
            // The refresh token validation will be handled by the controller
            // We just need to let the request pass through
            log.info("Refresh token endpoint called, skipping token validation");
            if (getAndSetUserAuthenticationInfo(request, response, token)) return;
        } else if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            if (getAndSetUserAuthenticationInfo(request, response, token)) return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean getAndSetUserAuthenticationInfo(HttpServletRequest request, HttpServletResponse response, String token) throws IOException {
        token = jwtTokenProvider.getTokenFromRequest(request);
        String username = jwtTokenProvider.getUsernameFromToken(token);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        if (jwtTokenProvider.validateToken(token, userDetails)) {
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Check access control
            String endpoint = request.getRequestURI();
            String method = request.getMethod();
            if (!userAccessService.isAccessAllowed(username, endpoint, method)) {
                log.info("Access denied for the user: {}", username);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
                return true;
            }
        }
        return false;
    }
}
