package com.vitraya.adjudication.engine.dto.enums;

public enum ClaimTransactionType {
    CASHLESS,
    REIMBURSEMENT;

    public static ClaimTransactionType getClaimTransactionType(String treatmentTypeStr) {
        for (ClaimTransactionType claimTransactionType : values()) {
            if (claimTransactionType.name().equals(treatmentTypeStr)) {
                return claimTransactionType;
            }
        }
        return null;
    }
}
