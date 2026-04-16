package com.vitraya.adjudication.engine.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class StatementsItem{
	private String booleanOperator;
	private List<StatementsItem> statements;
	private List<ValuesToFindItem> valuesToFind;
	private Object conditionFound;
	private List<Object> documentInclusionList;
	private int numberOfValues;
	private String entityName;
	private String conditionResult;
	private String operator;
	private List<Object> documentExclusionList;
	private ValueToCompare valueToCompare;
	private Object value;
	private String description;
}