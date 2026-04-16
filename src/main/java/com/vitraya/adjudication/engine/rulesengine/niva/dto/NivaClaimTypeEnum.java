package com.vitraya.adjudication.engine.rulesengine.niva.dto;

import lombok.Getter;

@Getter
public enum NivaClaimTypeEnum {
    NEW_PRE_AUTH(2), EXTENSION_PRE_AUTH(3), PENDING_PRE_AUTH(4), DISCHARGE_PRE_AUTH(10);;

    private final int claimTypeValue;

    NivaClaimTypeEnum(int claimTypeValue) {
        this.claimTypeValue = claimTypeValue;
    }

}
