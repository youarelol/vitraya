package com.vitraya.adjudication.engine.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ClaimResult {
    private List<String> coverageReasons;
    private List<String> coverageFailedReasons;
    private boolean claim_approved;
    private String procedure_covered;
    private String procedure_name;
    private BigDecimal claim_admissible_amount;
    private BigDecimal amount_before_avaiaable_si_check;
    private BigDecimal claim_approved_amount;
    private BigDecimal claim_approved_amount_after_ucr_application;
    private boolean is_si_capping_applied;
    private BigDecimal total_considered_si;
    private String claim_result_remarks;
    private boolean is_sublimit_less_than_si;
    private boolean is_sublimit_less_than_claimed_amount;
    private BigDecimal sublimit_amount_calc;
    private double percentage_drop;
    private double percentage_drop_rounded;
    private BigDecimal procedure_sublimit_applicable;

    public void addInCoverageReasons(String message) {
        if (this.coverageReasons == null)
            this.coverageReasons = new ArrayList<>();
        this.coverageReasons.add(message);
    }
}
