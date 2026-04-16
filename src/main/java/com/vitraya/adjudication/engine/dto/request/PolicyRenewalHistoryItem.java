package com.vitraya.adjudication.engine.dto.request;

import lombok.Data;

@Data
public class PolicyRenewalHistoryItem{
	private String renewalId;
	private String policyNumber;
	private String endDate;
	private String polSI;
	private String startDate;

	public String getRenewalId(){
		return renewalId;
	}

	public String getPolicyNumber(){
		return policyNumber;
	}

	public String getEndDate(){
		return endDate;
	}

	public String getPolSI(){
		return polSI;
	}

	public String getStartDate(){
		return startDate;
	}
}
