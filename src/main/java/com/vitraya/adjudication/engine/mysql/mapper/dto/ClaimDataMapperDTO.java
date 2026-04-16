package com.vitraya.adjudication.engine.mysql.mapper.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ClaimDataMapperDTO {
    private long id;
    private String intimationNumber;
    private long insuranceAgencyId;
    private long tpaId;
    private long hospitalId;
    private String hospitalName;
    private String claimType;
    private String patientName;
    private int patientAge;
    private String designation;
    private String currentPolicyEndDate;
    private String currentPolicyStartDate;
    private String reasonForHospitalization;
    private String dateOfBirth;
    private String coverCode;
    private String hospitalZone;
    private String status;
    private BigDecimal baseSumInsured;
    private BigDecimal remainingSumInsured;
    private String currentPolicyInceptionDate;
    private boolean active;
    private boolean deleted;
    private Date dateCreated;
    private Date dateUpdated;
    private String dateOfAdmission;
    private String dateOfDischarge;
    private String icdCode;
    private String policyRenewalHistory;
    private long procedureId;
    private String productCode;
    private String roomType;
    private String copayZone;
    private String policyVariant;
    private String treatmentType;
    private boolean assigned;
    private String assigned_by;
    private boolean pushedToInsurer;
    private String claimHistory;
    private String policyNumber;
    private String claimStatus;
    private String attendantMobileNumber;
    private String diagnosis;
    private String pedList;
    private String dateOfFirstDiagnosis;
    private String adjudicationStatus;
    private String medicalCardNumber;
    private String insurerIdentifier;
    private boolean enhancementProcessed;
    private boolean insurerVisible;
    private String initialDecision;
    private long initialTat;
    private String finalDecision;
    private long dischargeTat;
}
