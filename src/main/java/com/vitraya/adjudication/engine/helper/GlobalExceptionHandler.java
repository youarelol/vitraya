package com.vitraya.adjudication.engine.helper;

import com.vitraya.adjudication.engine.dto.response.RestAPIResponse;
import com.vitraya.adjudication.engine.service.CommunicationService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Value("${claim.registration.failed.email.to}")
    private String CLAIM_REGISTRATION_FAILED_EMAIL_TO;

    @Value("${notify.team}")
    private boolean notifyTeam;

    private final CommunicationService communicationService;

    public GlobalExceptionHandler(CommunicationService communicationService) {
        this.communicationService = communicationService;
    }

    @ExceptionHandler(VitrayaException.class)
    public ResponseEntity<Object> handleVitrayaException(VitrayaException ex) {
        log.error("Caught Vitraya exception in application as ", ex);
        if (notifyTeam) {
            communicationService.sendEmail("Vitraya Checked Exception ", ex.getMessage(), CLAIM_REGISTRATION_FAILED_EMAIL_TO);
        }
        return new ResponseEntity<>(RestAPIResponse.buildFail(HttpStatus.BAD_REQUEST.value(), "Validation Error",
                ex.getCode(), ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception e) {
        log.error("Caught exception in application as ", e);
        communicationService.sendEmail("Vitraya UnChecked Exception", e.getMessage(), CLAIM_REGISTRATION_FAILED_EMAIL_TO);
        return new ResponseEntity<>(RestAPIResponse.buildFail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Validation Error",
                String.valueOf(500), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Object> handleExpiredJwtException(ExpiredJwtException ex) {
        return new ResponseEntity<>(RestAPIResponse.buildFail(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Unauthorized: You don't have permission to access this resource.",
                        "401",
                        "Unauthorized: You don't have permission to access this resource.")
                .toString(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Object> handleJwtException(JwtException ex) {
        return new ResponseEntity<>(RestAPIResponse.buildFail(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Unauthorized: You don't have permission to access this resource.",
                        "401",
                        "Unauthorized: You don't have permission to access this resource.")
                .toString(), HttpStatus.UNAUTHORIZED);
    }
}
