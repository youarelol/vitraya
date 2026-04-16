package com.vitraya.adjudication.engine.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PolicyDetails{
	private BigDecimal cumulativeBonus;
	private BigDecimal availableSumInsured;
	private List<String> restorationDetails;
	private String installmentPendingAmount;
	private String policyNumber;
	private String policyInceptionDate;
	private String productName;
	private BigDecimal rechargeBenefitAmount;
	private String installmentYN;
	private BigDecimal sumInsured;
	private List<PolicyRenewalHistoryItem> policyRenewalHistory;
	private String policyPorted;
	private String nCDFlag;
	private String status64VB;
	private String nCDAmount;
	private int coPayPercentage;
	private String mobile;
	private String policyEndDate;
	private List<PreviousPolicyDetailsItem> previousPolicyDetails;
	private List<InsuredDetailsItem> insuredDetails;
	private String policyStartDate;
	private String installmentMethod;
	private String rtaSI;
	private String productCode;
	private String policyType;
	private String policyHolderName;
	private String restorationYN;
}