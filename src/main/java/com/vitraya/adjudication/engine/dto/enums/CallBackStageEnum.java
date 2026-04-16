package com.vitraya.adjudication.engine.dto.enums;

import com.vitraya.adjudication.engine.mysql.entity.ClaimData;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CallBackStageEnum {
    PREAUTH("PRE_AUTHORISATION_RESPONSE_SENT"),
    DUPLICATE_PREAUTH("DUPLICATE"),
    INTERIM("INTERIM_RESPONSE_SENT"),
    DISCHARGE("DISCHARGE_RESPONSE_SENT"),
    QUERY_REPLY("QUERY_REPLY_RESPONSE_SENT"),
    RECONSIDERATION("DENIAL_RECONSIDERATION_RESPONSE_SENT"),
    SETTLEMENT("SETTLEMENT_RESPONSE_SENT");

    private final String claimStatus;

    public static CallBackStageEnum mapClaimStage(String claimStatus) {
        for (CallBackStageEnum callBackStageEnum: CallBackStageEnum.values()) {
            if (callBackStageEnum.claimStatus.equalsIgnoreCase(claimStatus)) {
                return callBackStageEnum;
            }
        }
        return null;
    }
}
