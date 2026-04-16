package com.vitraya.adjudication.engine.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AmountsAfterPayables {
    private BigDecimal amountAfterProcedureConstruct;
    private BigDecimal amountForIrdaiPayable;
}