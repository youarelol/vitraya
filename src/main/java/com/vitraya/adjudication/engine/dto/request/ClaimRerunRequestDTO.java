package com.vitraya.adjudication.engine.dto.request;

import com.vitraya.adjudication.engine.dto.enums.ClaimRunIdentifier;
import lombok.Data;

@Data
public class ClaimRerunRequestDTO {
    private ClaimRunIdentifier claimRunIdentifier;
}
