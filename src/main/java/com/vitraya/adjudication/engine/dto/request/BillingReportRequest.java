package com.vitraya.adjudication.engine.dto.request;

import lombok.Data;

/**
 * Request model for billing report. Accepts optional start and end dates (yyyy-MM-dd).
 */
@Data
public class BillingReportRequest {
    private String startDate; // optional, format yyyy-MM-dd
    private String endDate;   // optional, format yyyy-MM-dd
}


