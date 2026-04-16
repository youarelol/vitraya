package com.vitraya.adjudication.engine.dto.request;

import lombok.Data;

@Data
public class ChronicIllnessListItem{
	private int illnessId;
	private String diagnosisDate;
	private int numberOfMonths;
	private String illnessIcdCode;
	private String illnessName;
}