package com.vitraya.adjudication.engine.dto.request;

import lombok.Data;

@Data
public class AuthRequestDTO {
    private String username;
    private String password;
    private String mobileNumber;
    private int corporateId;
    private String captchaToken;
}
