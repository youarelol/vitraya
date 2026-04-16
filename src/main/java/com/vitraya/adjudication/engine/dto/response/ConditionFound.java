package com.vitraya.adjudication.engine.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class ConditionFound{
	private String standardName;
	private List<Integer> valuesToFind;
	private boolean found;
	private boolean conditionResult;
	private String valueType;
	private List<String> valueUnit;
	private String entityId;
	private String operator;
	private int pageNumber;
	private String documentUrl;
	private String unitFound;
	private int pageHeight;
	private int pageWidth;
	private int referenceToDocument;
	private String name;
	private int x;
	private int width;
	private List<Object> valueFound;
	private int y;
	private int height;
}