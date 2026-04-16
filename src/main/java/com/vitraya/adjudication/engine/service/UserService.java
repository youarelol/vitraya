package com.vitraya.adjudication.engine.service;

import com.vitraya.adjudication.engine.config.jwt.JwtTokenProvider;
import com.vitraya.adjudication.engine.dto.enums.UserStatusEnum;
import com.vitraya.adjudication.engine.dto.enums.VitrayaErrorCodes;
import com.vitraya.adjudication.engine.dto.request.AuthRequestDTO;
import com.vitraya.adjudication.engine.dto.request.RegisterRequestDto;
import com.vitraya.adjudication.engine.dto.request.UserRequest;
import com.vitraya.adjudication.engine.dto.request.VerifyUserOtp;
import com.vitraya.adjudication.engine.dto.response.RecaptchaResponse;
import com.vitraya.adjudication.engine.dto.response.RestAPIResponse;
import com.vitraya.adjudication.engine.dto.response.UserDto;
import com.vitraya.adjudication.engine.helper.VitrayaException;
import com.vitraya.adjudication.engine.mysql.entity.LoginOtpTrail;
import com.vitraya.adjudication.engine.mysql.entity.Users;
import com.vitraya.adjudication.engine.mysql.repository.LoginOtpTrailRepo;
import com.vitraya.adjudication.engine.mysql.repository.RoleRepository;
import com.vitraya.adjudication.engine.mysql.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CommunicationService communicationService;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginOtpTrailRepo loginOtpTrailRepo;
    private final HelperService helperService;

    @Value("${recaptcha.secret:1231}")
    private String recaptchaSecret;

    @Value("${google.recaptcha.url:3422}")
    private String recaptchaUrl;

    @Value("${user.blocked.time:60}")
    private int blockedTime;

    @Value("${password.attempt.threshold:3}")
    private int passwordAttemptThreshold;

    @Value("${otp.validity.min}")
    private int otpValidityMin;


    @Value("${otp.sent.count.limit:3}")
    private int otpsentCountLimit;

    @Value("${otp.validity.time.min:1440}")
    private int getOtpValidityMin;

    @Value("${capthcha.verification.enabled}")
    private boolean captchaVerfificationEnabled;



    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, CommunicationService communicationService, JwtTokenProvider jwtTokenProvider, LoginOtpTrailRepo loginOtpTrailRepo, HelperService helperService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.communicationService = communicationService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.loginOtpTrailRepo = loginOtpTrailRepo;
        this.helperService = helperService;
    }

    public void registerUser(RegisterRequestDto registerRequest) {
        if (registerRequest == null || registerRequest.getUsername() == null) {
            throw new VitrayaException(VitrayaErrorCodes.INVALID_USER_REQUEST, new Object[]{});
        }
        Users existingUser = userRepository.findByUsername(registerRequest.getUsername())
                .orElse(null);
        if (existingUser != null) {
            log.info("User with username {} already exists", registerRequest.getUsername());
            throw new VitrayaException(VitrayaErrorCodes.INVALID_REQUEST, new Object[]{registerRequest.getUsername()});
        }
        Users user = new Users();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(helperService.generateRandomString())); //
        user.setEmail(registerRequest.getUsername()); //
        user.setMobileNumber("9999999999"); //
        user.setStatus(UserStatusEnum.ACTIVE);
        user.setPasswordAttempts(0);
        user.setCorporateId(registerRequest.getCorporateId());
        user.setCreatedBy("system");
        user.setCreatedOn(new Date());
        user.setUserid(helperService.generateRandomString());
        user.setRole(0);
        userRepository.save(user);
    }

    public Optional<Users> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void validateUserRequest(UserRequest userRequest) {

    }

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    public Map<String, Object> getClaimsMap(Users user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("userid", user.getUserid());

        return claims;
    }

    public String getAuthenticatedUsername() {
        User user = getUser();
        return user != null ? user.getUsername() : null;
    }

    private static User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null)
            return (User) authentication.getPrincipal();

        return null;
    }

    public Users getCurrentUser() {
        String username = getAuthenticatedUsername();
        log.info("Found the user as {}", username);

        if (username != null) {
            Optional<Users> user = findUserByUsername(username);
            if (user.isEmpty()) {
                throw new VitrayaException(VitrayaErrorCodes.INVALID_USER_REQUEST,
                        new Object[]{});
            }

            return user.get();
        }

        return null;
    }

    public Users findUserFromToken(String token) {
        return userRepository.findByToken(token);
    }

    public Users findUserFromRefreshToken(String refreshToken) {
        return userRepository.findByRefreshToken(refreshToken);
    }

    public void logoutUser(Users user) {
        log.info("Logging out user {}", user.getUsername());
        user.setToken(UUID.randomUUID().toString());
        userRepository.save(user);
    }

    public void saveUser(Users user) {
        userRepository.save(user);
    }

    public boolean verifyCaptcha(@Valid AuthRequestDTO authRequest) {
        if (!captchaVerfificationEnabled) {
            return true;
        }
        RestTemplate restTemplate = new RestTemplate();
        String params = String.format("?secret=%s&response=%s", recaptchaSecret, authRequest.getCaptchaToken());

        ResponseEntity<RecaptchaResponse> response = restTemplate.postForEntity(
                recaptchaUrl + params, null, RecaptchaResponse.class);

        return response.getBody() != null && response.getBody().isSuccess();
    }

    public boolean isUserBlocked(Users user) {
        if (user.getStatus() == UserStatusEnum.BLOCKED) {
            long blockDuration = System.currentTimeMillis() - user.getBlockTime().getTime();
            if (blockDuration >= blockedTime * 60 * 1000) {
                user.setStatus(UserStatusEnum.ACTIVE);
                user.setPasswordAttempts(0);
                user.setBlockTime(null);
                saveUser(user);
                return false;
            }
            return true;
        }
        return false;
    }

    public void handleFailedLoginAttempt(Users user) {
        int attempts = user.getPasswordAttempts() + 1;
        user.setPasswordAttempts(attempts);
        if (attempts >= passwordAttemptThreshold) {
            user.setStatus(UserStatusEnum.BLOCKED);
            user.setBlockTime(new Date());
        }
        saveUser(user);
    }

    public String getOtp(int length) {
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public Map getOtpData(Users user, String otp) {
        Map retmap = new HashMap();
        String randomString = helperService.generateRandomString();
        String email = user.getEmail();
        communicationService.sendEmail("OTP for Vitraya Login",
                "Your OTP is: " + otp + ". Use this to login to your account. Do not share it with anyone.",
                email);
        // String messageId = communicationService.sendSMS(mobileNumber, otp);

        retmap.put("otpIdentifier", randomString);
        retmap.put("otpmessage", "OTP has been sent on the registered email "
                + getMaskedEmail(email)
                + ". Please check your email to continue.");

        return retmap;
    }

    private String getMaskedEmail(String email) {
        return email.replaceAll("(?<=.{2}).(?=.*@)", "*");
    }

    public void saveOtpData(Users user, Map retmap, String otp) {
        String otpIdentifier = retmap.get("otpIdentifier").toString();
        user.setOtp(otp);
        user.setOtpIdentifier(otpIdentifier);
        user.setOtpCreated(new Date());
        userRepository.save(user);
        logOtpTrail(user);
    }

    public void logOtpTrail(Users user) {
        Date creationTime = user.getOtpCreated();
        Date expiryTime = new Date(creationTime.getTime() + ((long) otpValidityMin * 60 * 1000));

        LoginOtpTrail loginOtpTrail = new LoginOtpTrail();
        loginOtpTrail.setUserName(user.getUsername());
        loginOtpTrail.setMobileNumber(user.getMobileNumber());
        loginOtpTrail.setOtp(user.getOtp());
        loginOtpTrail.setMessageId(user.getOtpIdentifier());
        loginOtpTrail.setCreationTime(creationTime);
        loginOtpTrail.setExpiryTime(expiryTime);
        loginOtpTrail.setOtpConsumed(false);
        loginOtpTrailRepo.save(loginOtpTrail);
    }

    public Users verifyUserOtp(VerifyUserOtp verifyUserOtp) {
        Users user = userRepository.findByUsername(verifyUserOtp.getUsername()).orElse(null);
        if (user == null) {
            throw new VitrayaException(VitrayaErrorCodes.INVALID_USER_REQUEST,
                    new Object[]{});
        }
        if (!(user.getOtp() != null
                && user.getOtp().equalsIgnoreCase(verifyUserOtp.getOtp()))) {
            throw new VitrayaException(VitrayaErrorCodes.INCORRECT_OTP_RECEIVED);
        }
        if (!(user.getOtpIdentifier() != null
                && user.getOtpIdentifier().equalsIgnoreCase(verifyUserOtp.getOtpIdentifier()))) {
            throw new VitrayaException(VitrayaErrorCodes.INVALID_OTP_VERIFICATION_REQUEST);
        }
        Map<String, Object> claims = getClaimsMap(user);
        String token = jwtTokenProvider.generateAccessToken(verifyUserOtp.getUsername(), claims);
        String refreshToken = jwtTokenProvider.generateRefreshToken(verifyUserOtp.getUsername(), claims);
        user.setToken(token);
        user.setRefreshToken(refreshToken);
        user.setPasswordAttempts(0);
        saveUser(user);
        LoginOtpTrail loginOtpTrail = loginOtpTrailRepo.findByMessageIdAndOtp(verifyUserOtp.getOtpIdentifier(), verifyUserOtp.getOtp());
        if (loginOtpTrail != null) {
            if (loginOtpTrail.isOtpConsumed()) {
                LOGGER.info("message identifier - " + verifyUserOtp.getOtpIdentifier() + " otp - " + verifyUserOtp.getOtp() + " already consumed");
                throw new VitrayaException(VitrayaErrorCodes.INVALID_OTP_VERIFICATION_REQUEST);
            } else {
                if (loginOtpTrail.getExpiryTime().before(new Date())) {
                    LOGGER.info("otp - " + verifyUserOtp.getOtp() + "already consumed" + "for message identifier - " + verifyUserOtp.getOtpIdentifier());
                    throw new VitrayaException(VitrayaErrorCodes.INVALID_OTP_VERIFICATION_REQUEST);
                }
                loginOtpTrail.setOtpConsumed(true);
                loginOtpTrailRepo.save(loginOtpTrail);
            }
        }
        return user;
    }

    public String getLoggedCurrentUser() {
        String userId = "IFRAME";
        try {
            Users user = getCurrentUser();
            userId = user.getUserid();
        } catch (Exception e) {
            log.error("Error getting logged in user", e);
        }

        return userId;
    }

    public boolean isAuthorizedForUserDashboard(long userId) {
        return userRepository.isAuthorizedForUserDashboard(userId);
    }

    public ResponseEntity<RestAPIResponse> getDashboardUsers(int corporateId) {
        List<UserDto> dashBoardUsers = userRepository.findAllUserForDashboard(corporateId);
        return new ResponseEntity<>(RestAPIResponse.buildSuccess(dashBoardUsers), HttpStatus.OK);
    }

    public boolean verifyMultiOtpEligiblity(String mobileNumber) {
        int seconds = getOtpValidityMin * 60;
        LocalDateTime thresholdTime = LocalDateTime.now().minusSeconds(seconds);
        int otpCount = loginOtpTrailRepo.findTop3ForMobileNumber(mobileNumber, thresholdTime);

        if (otpCount >= otpsentCountLimit) {
            return false;
        }
        return true;
    }

}
