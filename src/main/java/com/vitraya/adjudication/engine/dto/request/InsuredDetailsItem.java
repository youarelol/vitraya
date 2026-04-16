package com.vitraya.adjudication.engine.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class InsuredDetailsItem{
	private Object cumulativeBonus;
	private String healthCardNumber;
	private Object sumInsured;
	private String inceptionDt;
	private String gender;
	private List<Object> restorationDetails;
	private List<PreExistingDiseasesItem> preExistingDiseases;
	private String dob;
	private Object coPayPercentage;
	private String insuredName;
	private String restorationYN;
	private String rtaSI;

	public Object getCumulativeBonus(){
		return cumulativeBonus;
	}

	public String getHealthCardNumber(){
		return healthCardNumber;
	}

	public Object getSumInsured(){
		return sumInsured;
	}

	public String getInceptionDt(){
		return inceptionDt;
	}

	public String getGender(){
		return gender;
	}

	public List<Object> getRestorationDetails(){
		return restorationDetails;
	}

	public List<PreExistingDiseasesItem> getPreExistingDiseases(){
		return preExistingDiseases;
	}

	public String getDob(){
		return dob;
	}

	public Object getCoPayPercentage(){
		return coPayPercentage;
	}

	public String getInsuredName(){
		return insuredName;
	}

	public String getRestorationYN(){
		return restorationYN;
	}

	public String getRtaSI(){
		return rtaSI;
	}
}