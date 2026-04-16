package com.vitraya.adjudication.engine.controller;


import com.vitraya.adjudication.engine.service.PolicyProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/policy")
public class PoliciesController {

    private final PolicyProductService policyProductService;

    public PoliciesController(PolicyProductService policyProductService) {
        this.policyProductService = policyProductService;
    }

    @GetMapping("/list")
    @Cacheable("policies")
    public Map<String, Object> getAllPolicies() {
        return policyProductService.getAllPolicies();
    }
}
