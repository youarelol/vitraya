package com.vitraya.adjudication.engine.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.util.List;

import java.util.List;

@Data
public class ProcedureRule {
    private String operator;
    private String booleanOperator;
    private List<StatementsItem> statements;
    private Integer numberOfValues;
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


    @Data
    public static class ProcedureGroup {
        private String name;
        private List<String> alias;
        private String id;
    }

}
