package com.vitraya.adjudication.engine.dto.response;

import com.google.gson.annotations.JsonAdapter;
import lombok.Data;

import java.util.List;

@Data
public class VneuronRuleCommonDTO {
    private String operator;
    private String booleanOperator;
    @JsonAdapter(StatementsItemAdapter.class)
    private List<StatementsItem> statements;
    private String numberOfValues;
    private String entityName;
    private List<ConditionFoundItem.ValuesToFindItem> valuesToFind;
    private List<Object> documentInclusionList;
    private List<Object> documentExclusionList;
    private String description;
    private List<ConditionFoundItem> conditionFound;
    private Boolean conditionResult;
    private Boolean adjudicationResult;
    private List<Float> range;
    private ValueToCompare valueToCompare;
    private Object value;

}
