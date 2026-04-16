package com.vitraya.adjudication.engine.controller;

import com.vitraya.adjudication.engine.dto.response.RestAPIResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


@Slf4j
@RestController
@RequestMapping("/api/v1/healthCheck")
public class HealthCheckController {
    @GetMapping("")
    public RestAPIResponse healthCheck() {
        return RestAPIResponse.buildSuccess("Application is up and running...");
    }
}
