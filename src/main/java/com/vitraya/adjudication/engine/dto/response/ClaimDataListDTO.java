package com.vitraya.adjudication.engine.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class ClaimDataListDTO {
    private int totalClaimCount;
    private int totalClaimCountAssigned;
    private List<ClaimDataDto> claimDataList;
    private List<ClaimDataDto> claimDataListAssigned;
    private boolean isMultiTabView = false;


    @Data
    public static class ClaimDataDto {
        private String id;
        private String intimationNumber;
        private String hospitalName;
        private String patientName;
        private String productCode;
        private boolean pushedToInsurer;
        private String claimStatus;
        private String adjudicationStatus;
        private String insurerIdentifier;
        private boolean insurerVisible;
        private String initialDecision;
        private long initialTat;
        private String finalDecision;
        private long dischargeTat;
        private BigDecimal preAuthBillAmount;
        private BigDecimal dischargeBillAmount;
        private BigDecimal preAuthAmountApproved;
        private BigDecimal dischargeAmountApproved;
        private String dischargeClaimDecision;
        private String preAuthDecision;
        private String status;
        private Date dateCreated;
        private Date dateUpdated;
        private BigDecimal billAmount;
        private BigDecimal approvedAmount;
        private String claimStatusText;
    }
}
