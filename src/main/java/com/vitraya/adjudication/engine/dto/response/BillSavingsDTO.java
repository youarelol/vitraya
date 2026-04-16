package com.vitraya.adjudication.engine.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BillSavingsDTO {
    private BigDecimal requestedAmount;
    private BigDecimal approvedAmount;
    private BigDecimal irdaiPayableSaving;
    private BigDecimal procedureConstructSaving;
    private BigDecimal pharmacySaving;
    private BigDecimal pureTariffSaving;
    private boolean isIrdaipayble;
    private int unit;

    private BigDecimal insurerRequestedAmount;
    private BigDecimal insurerApprovedAmount;
    private BigDecimal insurerIrdaiPayableSaving;
    private BigDecimal insurerProcedureConstructSaving;
    private BigDecimal insurerPharmacySaving;
    private BigDecimal insurerPureTariffSaving;
    private boolean isInsurerIrdaipayble;
    private int insurerUnit;

}
