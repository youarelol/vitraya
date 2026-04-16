package com.vitraya.adjudication.engine.dto.request;

import lombok.Data;

@Data
public class DiagnosisItem{
	private String itemName;
	private String icdCode;

	public String getItemName(){
		return itemName;
	}

	public String getIcdCode(){
		return icdCode;
	}
}
