package com.vitraya.adjudication.engine.dto.enums;

public enum ClaimType {
    PACKAGE, OPEN_BILL;

    public static ClaimType getClaimType(String claimTypeStr) {
        for (ClaimType claimType : values()) {
            if (claimType.name().equals(claimTypeStr)) {
                return claimType;
            }
        }
        return null;
    }
}
