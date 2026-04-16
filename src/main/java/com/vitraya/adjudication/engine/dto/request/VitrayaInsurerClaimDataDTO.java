package com.vitraya.adjudication.engine.dto.request;

import com.vitraya.adjudication.engine.dto.enums.ClaimRequestTypeEnum;
import lombok.Data;

@Data
public class VitrayaInsurerClaimDataDTO {
    private ClaimRequestDTO request;
    private String providerCode;
    private ClaimRequestTypeEnum requestType;
    private String payorCode;
    private String txnId;
}