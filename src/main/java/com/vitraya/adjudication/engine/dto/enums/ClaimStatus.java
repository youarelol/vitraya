package com.vitraya.adjudication.engine.dto.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ClaimStatus {
    CLAIM_CREATED("", "Claim Received"),
    CLAIM_INTIMATED("", "CLAIM_INTIMATED"),
    PRE_AUTHORISATION_RAISED("CLAIM_CREATED", "PRE_AUTHORISATION_RAISED"),
    INTERIM_RAISED("Interim Pre-Authorization Raised", "INTERIM_RAISED"),
    AL_ISSUED("", "AL_ISSUED"),
    DISCHARGE_RAISED("Discharge Pre-Authorization Raised", "DISCHARGE_RAISED"),
    SETTLEMENT_RAISED("Settlement Pre-Authorization Raised", "SETTLEMENT_RAISED"),
    SETTLEMENT_RESPONSE_SENT("Settlement Pre-Authorization response sent", "SETTLEMENT_RESPONSE_SENT"),
    SETTLEMENT_REPUSHED("Settlement Pre-Authorization repushed", "SETTLEMENT_RAISED"),
    QUERY_REPLY_RAISED("Query Reply Raised", "Initial Bill Entry raised for I3"),
    QUERY_REPLY_RESPONSE_SENT("Query Reply sent", "Initial Bill Entry raised for I3"),
    QUERY_FOR_PRE_AUTHORIZATION("Query Reply Received for Initial Pre authorization", "Initial Bill Entry raised for I3"),
    PRE_AUTH_QUERY_SEND_TO_PROVIDER("Query Reply Received for Initial Pre authorization", "Initial Bill Entry raised for I3"),
    AL_ON_HOLD_FOR_ADDITIONAL_INFORMATION("", "AL_ON_HOLD_FOR_ADDITIONAL_INFORMATION"),
    QUERY_FOR_INTERIM("Query Reply Received for Interim Pre authorization", "Interim Bill Entry raised for I3"),
    QUERY_FOR_DISCHARGE("Query Reply Received for Discharge Pre authorization", "Discharge Bill Entry raised for I3"),
    QUERY_FOR_SETTLEMENT("Query Reply Received for Settlement", "Settlement Bill Entry raised for I3"),
    DENIAL_RECONSIDERATION_RAISED("Denial Reconsideration", "Initial Bill Entry raised for I3"),
    DENIAL_RECONSIDERATION_REPUSHED("Denial Reconsideration", "Initial Bill Entry raised for I3"),
    DENIAL_RECONSIDERATION_RESPONSE_SENT("Denial Reconsideration repush", "Initial Bill Entry raised for I3"),
    PRE_AUTHORISATION_RESPONSE_SENT("Initial Bill Entry raised for I3", "Initial Bill Entry received from I3"),
    PRE_AUTHORISATION_RESPONSE_RE_PUSHED("Initial Bill Entry raised for I3", "Initial Bill Entry received from I3"),
    QUERY_REPLY_RE_PUSHED("Query Reply Received for Initial Pre authorization", "Initial Bill Entry raised for I3"),
    DUPLICATE("Initial Bill Entry raised for I3", "Initial Bill Entry received from I3"),
    INTERIM_RESPONSE_SENT("Interim Bill Entry raised for I3", "Interim Bill Entry received from I3"),
    INTERIM_RESPONSE_RE_PUSH("Interim Bill Entry raised for I3", "Interim Bill Entry received from I3"),
    DISCHARGE_RESPONSE_SENT("Discharge Bill Entry raised for I3", "Discharge Bill Entry received from I3"),
    DISCHARGE_RESPONSE_RE_PUSH("Discharge Bill Entry raised for I3", "Discharge Bill Entry received from I3");


    private final String previousStatus;
    private final String currentStatus;

    public static ClaimStatus getClaimStatus(String claimStatusStr, String previousStatusStr) {
        ClaimStatus claimStatus = null;
        ClaimStatus previousStatus = null;
        for (ClaimStatus claimStatusValue : values()) {
            if (claimStatusValue.toString().equalsIgnoreCase(claimStatusStr)) {
                return claimStatusValue;
            }
        }
        return claimStatus;
    }

    public static boolean isNotPreAuthStage(ClaimStatus claimStatus) {
        return isDischargeStage(claimStatus) || isInterimStage(claimStatus)
                || claimStatus.getCurrentStatus().equalsIgnoreCase(AL_ISSUED.getCurrentStatus());
    }

    public static boolean isDischargeStage(ClaimStatus claimStatus) {
        return ClaimStatus.DISCHARGE_RAISED == claimStatus
                || ClaimStatus.DISCHARGE_RESPONSE_SENT == claimStatus
                || ClaimStatus.DISCHARGE_RESPONSE_RE_PUSH == claimStatus;
    }

    public static boolean isInterimStage(ClaimStatus claimStatus) {
        return ClaimStatus.INTERIM_RAISED == claimStatus
                || ClaimStatus.INTERIM_RESPONSE_SENT == claimStatus
                || ClaimStatus.INTERIM_RESPONSE_RE_PUSH == claimStatus;
    }

    public static boolean isPreAuthStage(ClaimStatus claimStatus) {
        return ClaimStatus.PRE_AUTHORISATION_RAISED == claimStatus
                || ClaimStatus.PRE_AUTHORISATION_RESPONSE_SENT == claimStatus
                || ClaimStatus.CLAIM_CREATED == claimStatus
                || ClaimStatus.CLAIM_INTIMATED == claimStatus
                || ClaimStatus.PRE_AUTHORISATION_RESPONSE_RE_PUSHED == claimStatus
                || ClaimStatus.DUPLICATE == claimStatus;
    }

    public static boolean isPreAuthStageWithoutRepush(ClaimStatus claimStatus) {
        return ClaimStatus.PRE_AUTHORISATION_RAISED == claimStatus;
    }

    public static boolean isQueryStage(ClaimStatus claimStatus) {
        return ClaimStatus.QUERY_FOR_PRE_AUTHORIZATION == claimStatus
                || ClaimStatus.QUERY_FOR_INTERIM == claimStatus
                || ClaimStatus.QUERY_FOR_DISCHARGE == claimStatus
                || ClaimStatus.QUERY_REPLY_RE_PUSHED == claimStatus
                || ClaimStatus.QUERY_REPLY_RAISED == claimStatus;

    }

    public static boolean isReconsideration(ClaimStatus claimStatus) {
        return ClaimStatus.DENIAL_RECONSIDERATION_RESPONSE_SENT == claimStatus
                || ClaimStatus.DENIAL_RECONSIDERATION_REPUSHED == claimStatus
                || ClaimStatus.DENIAL_RECONSIDERATION_RAISED == claimStatus;
    }

    public static boolean isSettlement(ClaimStatus claimStatus) {
        return ClaimStatus.SETTLEMENT_RESPONSE_SENT == claimStatus
                || ClaimStatus.SETTLEMENT_REPUSHED == claimStatus
                || ClaimStatus.SETTLEMENT_RAISED == claimStatus
                ;
    }

    public static boolean isPreAuthQueryStage(ClaimStatus claimStatus) {
        return ClaimStatus.QUERY_FOR_PRE_AUTHORIZATION == claimStatus;
    }

    public static boolean isInterimQueryStage(ClaimStatus claimStatus) {
        return ClaimStatus.QUERY_FOR_INTERIM == claimStatus;
    }

    public static boolean isDischargeQueryStage(ClaimStatus claimStatus) {
        return ClaimStatus.QUERY_FOR_DISCHARGE == claimStatus;
    }

    public static boolean isNotDischargeCase(ClaimStatus claimStatus) {
        return ClaimStatus.isPreAuthStage(claimStatus) || ClaimStatus.isPreAuthQueryStage(claimStatus)
                || ClaimStatus.isInterimStage(claimStatus) || ClaimStatus.isInterimQueryStage(claimStatus);
    }

    public static ClaimStatus getResponseClaimStatus(ClaimStatus claimStatus) {
        if (isPreAuthStage(claimStatus))
            return PRE_AUTHORISATION_RESPONSE_SENT;
        else if (isInterimStage(claimStatus))
            return INTERIM_RESPONSE_SENT;
        else if (isDischargeStage(claimStatus))
            return DISCHARGE_RESPONSE_SENT;
        else if (isPreAuthQueryStage(claimStatus))
            return PRE_AUTHORISATION_RESPONSE_SENT;
        else if (isInterimQueryStage(claimStatus))
            return INTERIM_RESPONSE_SENT;
        else if (isDischargeQueryStage(claimStatus))
            return DISCHARGE_RESPONSE_SENT;
        return claimStatus;
    }

    private static boolean isNullOrBlank(String value) {
        return value == null || value.isEmpty() || value.equalsIgnoreCase("null");
    }

    public static String getPreviousStatusStr(ClaimStatus claimStatus) {
        return claimStatus.name();
        //todo: do we need to do this or not
        //return claimStatus.equals(PRE_AUTHORISATION_RAISED) ? "Initial Pre Authorization Raised" : claimStatus.getPreviousStatus();
    }
}
