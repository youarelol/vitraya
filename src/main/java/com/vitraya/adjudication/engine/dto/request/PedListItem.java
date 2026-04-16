package com.vitraya.adjudication.engine.dto.request;

import lombok.Data;

@Data
public class PedListItem{
	private String diagnosisDate;
	private String icdCode;
	private String numberOfMonths;
	private String illnessName;
	private boolean isCoded;
	private String name;
	private String iCDCode;

	public String getDiagnosisDate(){
		return diagnosisDate;
	}

	public String getIcdCode(){
		return icdCode;
	}

	public String getNumberOfMonths(){
		return numberOfMonths;
	}

	public String getIllnessName(){
		return illnessName;
	}

	public boolean isIsCoded(){
		return isCoded;
	}

	public String getName(){
		return name;
	}

	public String getICDCode(){
		return iCDCode;
	}
}
