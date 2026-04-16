package com.vitraya.adjudication.engine.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ClaimDTO {
    private int id;
    private int creatorId;
    private String createdDate;
    private int hospitalId;
    private String hospitalPatientId;
    private int insuranceAgencyId;
    private String dob;
    private String gender;
    private String medicalCardId;
    private String patientName;
    private String policyHolderName;
    private String policyNumber;
    private String policyType;
    private String policyInceptionDate;
    private String cityName;
    private String policyEndDate;
    private String policyName;
    private String policyStartDate;
    private String patientMobileNo;
    private String attendentMobileNo;
    private String patientEmailId;
    private String aadharDetails;
    private String medicalEventId;
    private String rohiniCode;
    private String intimationNumber;
    private String policySource;
    private BigDecimal sumInsured;
    private BigDecimal availableSumInsured;
    private String preAuthId;
    private boolean inScopeHospital;
    private boolean inScopeProcedure;
    private boolean inScopePolicy;
    private String claimStatusText;
    private String memberNo;
}