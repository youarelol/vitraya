package com.vitraya.adjudication.engine.dto.response;

import com.vitraya.adjudication.engine.dto.enums.CorporateTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private String status;
    private String role;
    private String userid;
    private CorporateTypeEnum type;
    private String roleName;
    private String refreshToken;
}
