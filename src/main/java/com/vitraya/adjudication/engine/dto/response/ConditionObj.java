package com.vitraya.adjudication.engine.dto.response;

import lombok.Data;

@Data
public class ConditionObj{
	private String name;
	private String entityId;
	private String standardName;
	private String documentUrl;
	private String fileName;
	private int referenceToDocument;
	private int pageNumber;
	private float x;
	private float y;
	private float width;
	private float height;
	private float pageWidth;
	private float pageHeight;
	private String foundIn;
	private boolean found;
	private String iCDCode;
	private String remarks;
	private String parent_category;
	private String medicalReasoning;
}