package com.vitraya.adjudication.engine.dto.request;

import lombok.Data;

@Data
public class PreviousPolicyDetailsItem{
	private String policyNo;
	private String uWYear;
	private String sumInsured;
	private String productName;
	private String productCode;
	private String insuredName;
	private String preExistingDisease;
	private String proposerName;
	private String policyFromDate;
	private String premium;
	private String previousInsurerName;
	private String policyToDate;

	public String getPolicyNo(){
		return policyNo;
	}

	public String getUWYear(){
		return uWYear;
	}

	public String getSumInsured(){
		return sumInsured;
	}

	public String getProductName(){
		return productName;
	}

	public String getProductCode(){
		return productCode;
	}

	public String getInsuredName(){
		return insuredName;
	}

	public String getPreExistingDisease(){
		return preExistingDisease;
	}

	public String getProposerName(){
		return proposerName;
	}

	public String getPolicyFromDate(){
		return policyFromDate;
	}

	public String getPremium(){
		return premium;
	}

	public String getPreviousInsurerName(){
		return previousInsurerName;
	}

	public String getPolicyToDate(){
		return policyToDate;
	}
}
