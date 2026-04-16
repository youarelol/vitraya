package com.vitraya.adjudication.engine.dto.enums;

public enum ClaimVNeuronResponseStatus {
    NOT_APPLICABLE,
    INVALID_DATA_RECEIVED,
    ACCEPTED,
    REJECTED,
    QUERY,
    PENDING,
    SUSPENDED,
    REJECT,
    ERROR,
    RECOMMENDED_FOR_MANUAL_VERIFICATION;

    public static ClaimVNeuronResponseStatus getDecision(String adjudicationResult) {
        // Based on the value received we need to return the proper response.
        if (adjudicationResult == null) {
            return PENDING;
        }

        for (ClaimVNeuronResponseStatus response : ClaimVNeuronResponseStatus.values()) {
            if (response.name().equalsIgnoreCase(adjudicationResult)) {
                return response;
            }
        }

        return PENDING;
    }
}
