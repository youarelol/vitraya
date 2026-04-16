package com.vitraya.adjudication.engine.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class Metadata{
	private String insurer;
	private String procedure;
	private String room_type;
	private List<Integer> bill_pages;
	private String hospital_code;
	private String procedure_code;
	private int no_of_line_items;
	private PatientMetaData patient_meta_data;
	private String unique_identifier;
	private boolean percentage_rule_applied;
	private HospitalClaimMetadata hospital_claim_metadata;
	private String bill_s3_url;
	private List<String> billS3UrlList;
}