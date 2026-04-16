package com.vitraya.adjudication.engine.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BenefitResult {
    private List<String> coverageReasons;
    private List<String> coverageFailedReasons;
    private BigDecimal benefit_group_claimed_amount;
    private boolean benefit_covered;
    private String benefit_group;
    private BigDecimal benefit_group_admissible_amount;
    private BigDecimal LimitValue;
    private BigDecimal copay;
    private BigDecimal amount_before_copay;
    private boolean drop_applicable;
    private BigDecimal amount_after_drop;
    private BigDecimal hospitalPayableDeduction;
    private BigDecimal patientPayableDeduction;
    private boolean updated;
    private String remarks;
}
