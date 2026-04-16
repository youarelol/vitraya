package com.vitraya.adjudication.engine.dto.request;

import lombok.Data;

@Data
public class ClaimAdmissionDetailsDTO {
	private int claimId;
	private String admissionDate;
	private String dischargeDate;
	private boolean icuStay;
	private CostEstimateDTO costEstimate;
	private String roomType;
}