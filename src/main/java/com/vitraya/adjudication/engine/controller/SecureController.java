package com.vitraya.adjudication.engine.controller;

import com.vitraya.adjudication.engine.dto.enums.ClaimDataParseMapping;
import com.vitraya.adjudication.engine.service.ClaimDataRequestService;
import com.vitraya.adjudication.engine.service.factory.ClaimsFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/secure")
public class SecureController {

    private final ClaimsFactory claimServiceFactory;

    public SecureController(ClaimsFactory claimServiceFactory) {
        this.claimServiceFactory = claimServiceFactory;
    }

    @GetMapping(value = "")
    public String getSecureText() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(authentication.getDetails());
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        ClaimDataRequestService claimProcessingService = claimServiceFactory.getClaimsFactory(ClaimDataParseMapping.DEFAULT_MAPPING);

        return "Secure String " + userDetails.getUsername();
    }
}
