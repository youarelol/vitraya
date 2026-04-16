package com.vitraya.adjudication.engine.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class DiagnosisRule{
	private String operator;
	private String entityName;
	private List<ValuesToFindItem> valuesToFind;
	private List<Object> documentInclusionList;
	private List<Object> documentExclusionList;
	private String description;
	@JsonIgnore
	private List<ConditionFoundItem> conditionFound;
	private boolean conditionResult;
	private boolean adjudicationResult;
}