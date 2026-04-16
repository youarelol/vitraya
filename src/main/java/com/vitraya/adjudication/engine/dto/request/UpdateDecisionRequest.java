package com.vitraya.adjudication.engine.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateDecisionRequest {
    private String claimNumber;
    private String claimDataIdStr;
    private long claimDataId;
    private String status;
    private String remarks;
    private BigDecimal amountApproved;
    private String queryText;
}
