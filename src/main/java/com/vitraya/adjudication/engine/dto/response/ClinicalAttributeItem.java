package com.vitraya.adjudication.engine.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class ClinicalAttributeItem{
	private String name;
	private String entityId;
	private List<Object> valueFound;
	private String unitFound;
	private String standardName;
	private String operator;
	private List<Object> valuesToFind;
	private String valueType;
	private String documentUrl;
	private int referenceToDocument;
	private int pageNumber;
	private int x;
	private int y;
	private int width;
	private int height;
	private int pageWidth;
	private int pageHeight;
	private boolean found;
	private boolean conditionResult;
}