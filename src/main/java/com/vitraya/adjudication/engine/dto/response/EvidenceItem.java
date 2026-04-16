package com.vitraya.adjudication.engine.dto.response;

import lombok.Data;

@Data
public class EvidenceItem{
	private String name;
	private String entityId;
	private String standardName;
	private String documentUrl;
	private int referenceToDocument;
	private int pageNumber;
	private int x;
	private int y;
	private int width;
	private int height;
	private int pageWidth;
	private int pageHeight;
	private String foundIn;
	private boolean found;
}