package com.vitraya.adjudication.engine.dto.response;

import lombok.Data;

@Data
public class DocumentItem{
	private String name;
	private String standardName;
	private String entityId;
	private int referenceToDocument;
	private int pageNumber;
	private boolean found;
	private String classifiedBy;
	private String documentUrl;
	private int pageHeight;
	private int pageWidth;
	private int width;
	private int height;
	private int x;
	private int y;
	private String remarks;
}