package com.vitraya.adjudication.engine.dto.enums;

public enum DocClaimStage {
    PRE_AUTH,
    INTERIM,
    DISCHARGE,
    PRE_AUTH_QUERY,
    INTERIM_QUERY,
    DISCHARGE_QUERY,
    QUERY,
    SETTLEMENT,
    SETTLEMENT_QUERY,
    QUERY_REPLIED,
    RECONSIDERATION;

    public static DocClaimStage getStage(ClaimStatus claimStatus) {
        if (ClaimStatus.isPreAuthStage(claimStatus))
            return PRE_AUTH;
        else if (ClaimStatus.isInterimStage(claimStatus))
            return INTERIM;
        else if (ClaimStatus.isDischargeStage(claimStatus))
            return DISCHARGE;
        else if (ClaimStatus.isQueryStage(claimStatus))
            return QUERY;
        else if (ClaimStatus.isSettlement(claimStatus))
            return SETTLEMENT;
        else if (ClaimStatus.isReconsideration(claimStatus))
            return RECONSIDERATION;

        return null;
    }

    public static DocClaimStage getTariffStage(ClaimStatus claimStatus) {
        if (ClaimStatus.isPreAuthStage(claimStatus))
            return PRE_AUTH;
        else if (ClaimStatus.isInterimStage(claimStatus))
            return INTERIM;
        else if (ClaimStatus.isDischargeStage(claimStatus))
            return DISCHARGE;
        else if (ClaimStatus.isPreAuthQueryStage(claimStatus)
                || ClaimStatus.isInterimQueryStage(claimStatus)
                || ClaimStatus.isDischargeQueryStage(claimStatus))
            return QUERY;
        return PRE_AUTH;
    }
}
