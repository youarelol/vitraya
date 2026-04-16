package com.vitraya.adjudication.engine.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Row model for the billing report. Populate these fields based on business logic,
 * then the Excel helper will render them in the specified column order.
 */
@Data
@Builder
public class BillingReportRow {
    private String claimReceivedDateTime;
    private String claimCreatedDateTime;
    private String claimSentToNivaDateTime;
    private String vitrayaClaimId;
    private String nivaPreauthId;
    private String hospitalCode;
    private String hospitalName;
    private String patientName;
    private String policyName;
    private String policyNumber;
    private String memberNumber;
    private String procedure;
    private String claimStage;
    private String vitrayaClaimStatus;
    private String nivaClaimStatus;
    private String modificationRemark;
    private String icdCode;
    private BigDecimal hospitalRequestedAmount;
    private BigDecimal vitrayaApprovedAmount;
    private BigDecimal nivaApprovedAmount;
    private BigDecimal differenceInApprovedAmount;
    private BigDecimal vitrayaSavingsAmount;
    private String vitrayaSavingsPercentage;
    private BigDecimal nivaRealisedSavingsAmount;
    private BigDecimal nivaRealisedSavingsPercentage;
    private BigDecimal differenceInSavingsAmount;
    private BigDecimal differenceInSavingsPercentage;
    private BigDecimal vitrayaTotalTariffSavingsAmount;
    private BigDecimal vitrayaTotalTariffSavingsPercentage;
    private BigDecimal nivaTotalTariffSavingsAmount;
    private BigDecimal nivaTotalTariffSavingsPercentage;
    private BigDecimal vitrayaPureTariffSavingsAmount;
    private BigDecimal vitrayaPureTariffSavingsPercentage;
    private BigDecimal nivaPureTariffSavingsAmount;
    private BigDecimal nivaPureTariffSavingsPercentage;
    private BigDecimal vitrayaNMESavingsAmount;
    private BigDecimal vitrayaNMESavingsPercentage;
    private BigDecimal nivaNMESavingsAmount;
    private BigDecimal nivaNMESavingsPercentage;
    private BigDecimal vitrayaPharmacySavingsAmount;
    private BigDecimal vitrayaPharmacySavingsPercentage;
    private BigDecimal nivaPharmacySavingsAmount;
    private BigDecimal nivaPharmacySavingsPercentage;
    private BigDecimal vitrayaPmlSavingsAmount;
    private BigDecimal vitrayaPmlSavingsPercentage;
    private BigDecimal nivaPmlSavingsAmount;
    private BigDecimal nivaPmlSavingsPercentage;
    private BigDecimal vitrayaVneuronSavingsAmount;
    private BigDecimal vitrayaVneuronSavingsPercentage;
    private BigDecimal nivaVneuronSavingsAmount;
    private BigDecimal nivaVneuronSavingsPercentage;
    private BigDecimal anonymousSavings;
    private BigDecimal hospitalPayableDeductionsManuallyAddedByInsurer;
    private BigDecimal patientPayableDeductionsManuallyAddedByInsured;
    private String inscopeClaim;
    private boolean emailClaim;
    private String tariffApplied;
    private BigDecimal amountMatchPercentage;
}



