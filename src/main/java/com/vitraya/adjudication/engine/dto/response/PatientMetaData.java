package com.vitraya.adjudication.engine.dto.response;

import lombok.Data;

@Data
public class PatientMetaData{
	private String bed_type;
	private String bill_date;
	private String room_type;
	private String room_category;
	private String admission_date;
	private String billing_number;
	private String discharge_date;
}