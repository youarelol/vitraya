package com.vitraya.adjudication.engine.dto.response;

import lombok.Data;

@Data
public class VCVMTokenResponse {
    private String access_token;
    private String token_type;
    private String organization;
}
