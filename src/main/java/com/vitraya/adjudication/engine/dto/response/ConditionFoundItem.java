package com.vitraya.adjudication.engine.dto.response;

import lombok.Data;

@Data
public class ConditionFoundItem{
	private String name;
	private String standardName;
	private String evidenceText;
	private String medicalReasoning;
	private String entityName;
	private Integer pageNumber;
	private String documentType;
	private Boolean found;
	private float x;
	private float y;
	private float width;
	private float height;
	private float pageWidth;
	private float pageHeight;
	private String remarks;
	private String parent_category;


	@Data
	public static class ValuesToFindItem {
		private String id;
		private String name;
		private String procedure_code;
		private ProcedureRule.ProcedureGroup fk_procedure_group;
		private String SNOMEDCT_CODE;
		private String snomedct_code;
	}


}