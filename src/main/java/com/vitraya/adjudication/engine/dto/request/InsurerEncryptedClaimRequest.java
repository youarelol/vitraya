package com.vitraya.adjudication.engine.dto.request;

import lombok.Data;

@Data
public class InsurerEncryptedClaimRequest {
    private String vitrayaClaimId;
    private String reviewer;
    private String insurerCode;
}
