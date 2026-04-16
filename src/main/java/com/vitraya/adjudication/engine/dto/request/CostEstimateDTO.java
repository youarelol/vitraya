package com.vitraya.adjudication.engine.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CostEstimateDTO {
    private BigDecimal totalCost;
    private BigDecimal packageAmount;
    private List<EstimatesItem> estimates;
    private BigDecimal authorizedCost;
    private BigDecimal amountEligibleForCoverage;
    private BigDecimal coPayDeduction;
    private BigDecimal zoneBasedDeduction;
    private BigDecimal totalDeduction;
    private BigDecimal payableByInsurer;
    private BigDecimal payableByPatient;
    private BigDecimal amountAbovePackage;
    private BigDecimal amountNotPayableTreatment;
    private BigDecimal proportionateDeduction;
    private BigDecimal mouDiscount;
    private BigDecimal upperLimit;
    private String upperLimitRuleMessage;
    private BigDecimal totalAmountParsedFromBill;
    private BigDecimal packageCappingCost;
    private BigDecimal packageApprovedCost;
    private String packageRuleMessage;
    private BigDecimal requestedPackageCost;
    private String authorisedAmountRuleMessage;
}