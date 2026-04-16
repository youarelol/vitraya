package com.vitraya.adjudication.engine.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CategorySummaryAmountDetails {
    private BigDecimal deductions;
    private BigDecimal requested_amount;
    private BigDecimal admissible_amount;
    private BigDecimal admissible_amount_without_procedure_construct;
    private BigDecimal amount_for_irdai_payable;
    private BigDecimal amount_after_procedure_construct;
}
