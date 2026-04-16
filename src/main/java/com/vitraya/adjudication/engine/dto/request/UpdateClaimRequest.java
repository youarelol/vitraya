package com.vitraya.adjudication.engine.dto.request;


import lombok.Data;

@Data
public class UpdateClaimRequest {
    private String claimId;
    private String icdCode;
    private String roomType;
    private String procedure;
    private String dateOfAdmission;
    private String dateOfDischarge;

}
