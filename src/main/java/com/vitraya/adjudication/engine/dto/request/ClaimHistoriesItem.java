package com.vitraya.adjudication.engine.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ClaimHistoriesItem{
	private String hospitilizationDate;
	private String treatmentType;
	private BigDecimal utilisedAmount;
	private List<String> procedures;
	private String healthId;
	private BigDecimal paidAmount;
	private BigDecimal provisionAmount;
	private String claimNumber;
	private List<String> illness;
}