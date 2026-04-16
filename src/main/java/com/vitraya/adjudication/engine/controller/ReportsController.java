package com.vitraya.adjudication.engine.controller;

import com.vitraya.adjudication.engine.dto.request.BillingReportRequest;
import com.vitraya.adjudication.engine.dto.response.RestAPIResponse;
import com.vitraya.adjudication.engine.service.BillingReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
public class ReportsController {

    private final BillingReportService billingReportService;

    public ReportsController(BillingReportService billingReportService) {
        this.billingReportService = billingReportService;
    }

    @PostMapping("/billing")
    public ResponseEntity<RestAPIResponse> billingReport(@RequestBody BillingReportRequest request) {
        log.info("Received billing report request for date range: {} to {}",
                request.getStartDate(), request.getEndDate());
        // Submit the report generation asynchronously
        billingReportService.generateAndEmailReportAsync(request);

        return ResponseEntity.ok(RestAPIResponse.buildSuccess("Your request has been submitted successfully to generate the billing report. You will receive the report via email once it's ready."));
    }
}
