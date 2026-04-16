package com.vitraya.adjudication.engine.controller;

import com.vitraya.adjudication.engine.config.jwt.JwtTokenProvider;
import com.vitraya.adjudication.engine.dto.enums.VitrayaErrorCodes;
import com.vitraya.adjudication.engine.dto.request.*;
import com.vitraya.adjudication.engine.dto.response.AuthResponse;
import com.vitraya.adjudication.engine.dto.response.RestAPIResponse;
import com.vitraya.adjudication.engine.mysql.entity.Corporate;
import com.vitraya.adjudication.engine.mysql.entity.Role;
import com.vitraya.adjudication.engine.mysql.entity.Users;
import com.vitraya.adjudication.engine.mysql.repository.RoleRepository;
import com.vitraya.adjudication.engine.service.CorporateService;
import com.vitraya.adjudication.engine.service.IframeService;
import com.vitraya.adjudication.engine.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final CorporateService corporateService;
    private final RoleRepository roleRepository;
    private final IframeService iframeService;

    @Value("${otp.enabled}")
    private boolean otpEnabled;

    @Value("${otp.length}")
    private int otpLength;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider,
                          UserService userService, CorporateService corporateService, IframeService iframeService, RoleRepository roleRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.corporateService = corporateService;
        this.roleRepository = roleRepository;
        this.iframeService = iframeService;
    }

    @PostMapping("/login")
    public ResponseEntity<RestAPIResponse> login(@RequestBody AuthRequestDTO authRequest) {
        try {
            boolean isCaptchaVerified = userService.verifyCaptcha(authRequest);
            if (!isCaptchaVerified) {
                return ResponseEntity.status(401).body(RestAPIResponse.buildFail(401, "Invalid credentials",
                        VitrayaErrorCodes.ERROR_INVALID_CREDENTIALS.getCode(), ""));
            }

            Users user = userService.findUserByUsername(authRequest.getUsername()).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401).body(RestAPIResponse.buildFail(401, "Invalid credentials",
                        VitrayaErrorCodes.ERROR_INVALID_CREDENTIALS.getCode(), ""));
            }

            if (user != null && userService.isUserBlocked(user)) {
                return ResponseEntity.status(403).body(RestAPIResponse.buildFail(403, "User is blocked. Try again later.",
                        VitrayaErrorCodes.ERROR_USER_BLOCKED.getCode(), ""));
            }

            /*authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), "")
            );*/

            Map retmap;
            if (otpEnabled) {
                if (!userService.verifyMultiOtpEligiblity(user.getMobileNumber())) {
                    return ResponseEntity.status(401).body(RestAPIResponse.buildFail(401, "Otp Limit Exceeded. Try again later.",
                            VitrayaErrorCodes.OTP_LIMIT_EXCEEDED.getCode(), ""));
                }
                String otp = userService.getOtp(otpLength);
                retmap = userService.getOtpData(user, otp);
                retmap.put("otpFlow", true);
                userService.saveOtpData(user, retmap, otp);
                return ResponseEntity.ok(RestAPIResponse.buildSuccess(retmap));
            } else {
                Corporate corporate = corporateService.getUserCorporate(user.getCorporateId());
                Map<String, Object> claims = userService.getClaimsMap(user);
                String token = jwtTokenProvider.generateAccessToken(authRequest.getUsername(), claims);
                String refreshToken = jwtTokenProvider.generateRefreshToken(authRequest.getUsername(), claims);

                // Save token in user table for authentication.
                user.setToken(token);
                user.setRefreshToken(refreshToken);
                user.setPasswordAttempts(0);
                userService.saveUser(user);
                Optional<Role> role = roleRepository.findById((long) user.getRole());
                return ResponseEntity
                        .ok(RestAPIResponse
                                .buildSuccess(new AuthResponse(token, user.getEmail(), user.getStatus().toString(),
                                        String.valueOf(user.getRole()), user.getUserid(), corporate.getType(),
                                        role.isPresent() ? role.get().getRoleName() : null, refreshToken))
                        );
            }
        } catch (AuthenticationException e) {
            log.error("Caught exception while validating the user as", e);
            Users user = userService.findUserByUsername(authRequest.getUsername()).orElse(null);
            if (user != null) {
                userService.handleFailedLoginAttempt(user);
            }
            return ResponseEntity.status(401).body(RestAPIResponse.buildFail(401, "Invalid credentials",
                    VitrayaErrorCodes.ERROR_INVALID_CREDENTIALS.getCode(), ""));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<RestAPIResponse> register(@RequestBody RegisterRequestDto registerRequest) {
        userService.registerUser(registerRequest);
        return ResponseEntity.ok(RestAPIResponse.buildSuccess("User registered successfully"));
    }

    @PostMapping("/check")
    public ResponseEntity<RestAPIResponse> check() {
        Users user = userService.getCurrentUser();
        Corporate corporate = corporateService.getUserCorporate(user.getCorporateId());
        Optional<Role> role = roleRepository.findById((long) user.getRole());

        return ResponseEntity
                .ok(RestAPIResponse
                        .buildSuccess(new AuthResponse("", user.getEmail(), user.getStatus().toString(),
                                String.valueOf(user.getRole()), user.getUserid(), corporate.getType(),
                                role.isPresent() ? role.get().getRoleName() : null, user.getRefreshToken()))
                );
    }

    @PostMapping("/logout")
    public ResponseEntity<RestAPIResponse> logout() {
        Users user = userService.getCurrentUser();
        userService.logoutUser(user);
        return ResponseEntity
                .ok(RestAPIResponse
                        .buildSuccess(
                                "User logged out successfully"
                        )
                );
    }

    @PostMapping("/verify/otp")
    public ResponseEntity<RestAPIResponse> verifyOtp(@Valid @RequestBody VerifyUserOtp verifyUserOtp) throws Exception {
        verifyUserOtp.validate();
        Users user = userService.verifyUserOtp(verifyUserOtp);
        if (user != null) {
            Optional<Role> role = roleRepository.findById((long) user.getRole());
            Corporate corporate = corporateService.getUserCorporate(user.getCorporateId());
            return ResponseEntity
                    .ok(RestAPIResponse
                            .buildSuccess(new AuthResponse(user.getToken(), user.getEmail(), user.getStatus().toString(),
                                    String.valueOf(user.getRole()), user.getUserid(), corporate.getType(),
                                    role.isPresent() ? role.get().getRoleName() : null, user.getRefreshToken()))
                    );
        } else {
            return ResponseEntity
                    .ok(RestAPIResponse
                            .buildFail(200, "OTP verification failed",
                                    VitrayaErrorCodes.INVALID_OTP_VERIFICATION_REQUEST.getCode(),
                                    "OTP verification failed")
                    );
        }
    }

    @PostMapping("/users")
    public ResponseEntity<RestAPIResponse> getAllUser() {
        Users user = userService.getCurrentUser();
        boolean isAuthorized = userService.isAuthorizedForUserDashboard(user.getId());
        if (isAuthorized) {
            return userService.getDashboardUsers(user.getCorporateId());
        }
        return new ResponseEntity<>(RestAPIResponse.buildFail(200, "Unauthorized", VitrayaErrorCodes.ERROR_INVALID_CREDENTIALS.getCode(), "Unauthorized"), HttpStatus.UNAUTHORIZED);
    }


    @PostMapping("/validate/insurer/token")
    public ResponseEntity<RestAPIResponse> validateInsurerToken(@RequestBody String encryptedToken) throws Exception {
        encryptedToken = URLDecoder.decode(encryptedToken, StandardCharsets.UTF_8).replaceAll(" ", "+");
        InsurerEncryptedClaimRequest insurerEncryptedClaimRequest = iframeService.getDecryptedIframeData(encryptedToken);
        if (insurerEncryptedClaimRequest != null && insurerEncryptedClaimRequest.getReviewer() != null
                && !insurerEncryptedClaimRequest.getReviewer().isEmpty()) {

            Users user = userService.findUserByUsername(insurerEncryptedClaimRequest.getReviewer() + "@nivabupa.com").orElse(null);
            try {
                if (user == null) {
                    return ResponseEntity.status(401).body(RestAPIResponse.buildFail(401, "Invalid credentials",
                            VitrayaErrorCodes.ERROR_INVALID_CREDENTIALS.getCode(), ""));
                }

                if (user != null && userService.isUserBlocked(user)) {
                    return ResponseEntity.status(403).body(RestAPIResponse.buildFail(403, "User is blocked. Try again later.",
                            VitrayaErrorCodes.ERROR_USER_BLOCKED.getCode(), ""));
                }

//                authenticationManager.authenticate(
//                        new UsernamePasswordAuthenticationToken(user.getUsername(), "C0sm!c$R@!nb0w#4&xP")
//                );

                Corporate corporate = corporateService.getUserCorporate(user.getCorporateId());
                Map<String, Object> claims = userService.getClaimsMap(user);
                String token = jwtTokenProvider.generateAccessToken(user.getUsername(), claims);
                String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername(), claims);
                // Save token in user table for authentication.
                user.setToken(token);
                user.setRefreshToken(refreshToken);
                user.setPasswordAttempts(0);
                userService.saveUser(user);

                return ResponseEntity
                        .ok(RestAPIResponse
                                .buildSuccess(
                                        new AuthResponse(token, user.getEmail(), user.getStatus().toString(),
                                                String.valueOf(user.getRole()), user.getUserid(), corporate.getType(),
                                                null, refreshToken)
                                )
                        );
            } catch (AuthenticationException e) {
                log.error("Caught exception while validating the user as", e);
                if (user != null) {
                    userService.handleFailedLoginAttempt(user);
                }
                return ResponseEntity.status(401).body(RestAPIResponse.buildFail(401, "Invalid credentials",
                        VitrayaErrorCodes.ERROR_INVALID_CREDENTIALS.getCode(), ""));
            }
        }

        return ResponseEntity
                .ok(RestAPIResponse
                        .buildFail(400, "Bad request",
                                VitrayaErrorCodes.INVALID_CLAIM_DATA_REQUEST.getCode(),
                                "Bad request")
                );
    }

    @PostMapping("/check/refresh-token")
    public ResponseEntity<RestAPIResponse> checkRefreshToken(@RequestBody RefreshTokenValidateRequest refreshTokenValidateRequest) throws Exception {
        log.info("Checking refresh token: {}", refreshTokenValidateRequest.getRefreshToken());

        // Validate the refresh token
        if (refreshTokenValidateRequest.getRefreshToken() == null || refreshTokenValidateRequest.getRefreshToken().isEmpty()) {
            return ResponseEntity
                    .status(401)
                    .body(RestAPIResponse
                            .buildFail(401, "Invalid refresh token",
                                    VitrayaErrorCodes.ERROR_INVALID_CREDENTIALS.getCode(),
                                    "Refresh token is missing or empty")
                    );
        }

        // Find the user by refresh token
        Users user = userService.findUserFromRefreshToken(refreshTokenValidateRequest.getRefreshToken());
        if (user == null) {
            return ResponseEntity
                    .status(401)
                    .body(RestAPIResponse
                            .buildFail(401, "Invalid refresh token",
                                    VitrayaErrorCodes.ERROR_INVALID_CREDENTIALS.getCode(),
                                    "Refresh token is invalid or expired")
                    );
        }

        // Check if the user is blocked
        if (userService.isUserBlocked(user)) {
            return ResponseEntity
                    .status(403)
                    .body(RestAPIResponse
                            .buildFail(403, "User is blocked. Try again later.",
                                    VitrayaErrorCodes.ERROR_USER_BLOCKED.getCode(), "")
                    );
        }

        // Generate new tokens
        Map<String, Object> claims = userService.getClaimsMap(user);
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), claims);

        // Update user with new tokens
        user.setToken(newAccessToken);
        user.setRefreshToken(refreshTokenValidateRequest.getRefreshToken());
        userService.saveUser(user);

        // Get corporate and role information
        Corporate corporate = corporateService.getUserCorporate(user.getCorporateId());
        Optional<Role> role = roleRepository.findById((long) user.getRole());

        // Return the new tokens
        return ResponseEntity
                .ok(RestAPIResponse
                        .buildSuccess(new AuthResponse(newAccessToken, user.getEmail(), user.getStatus().toString(),
                                String.valueOf(user.getRole()), user.getUserid(), corporate.getType(),
                                role.isPresent() ? role.get().getRoleName() : null, refreshTokenValidateRequest.getRefreshToken()))
                );
    }
}
