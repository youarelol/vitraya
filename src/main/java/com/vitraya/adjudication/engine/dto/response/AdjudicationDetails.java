package com.vitraya.adjudication.engine.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

@Data
public class AdjudicationDetails {
    private boolean primary;
    private String name;
    private String ruleId;
    private Object referenceProcedureCode;
    private QueryResponse queryResponse;
    private String adjudicationReason;
    private Object procedureProtocolQuestions;
    private List<ConditionObj> procedures;
    private List<ConditionObj> diagnoses;
    private List<ConditionObj> evidence;
    private List<ConditionObj> document;
    private List<Object> tissue;
    private List<Object> drug;
    private List<ConditionObj> clinicalAttribute;
    private String adjudicationResult;
    private VneuronRuleCommonDTO procedureRule;
    private VneuronRuleCommonDTO diagnosisRule;
    private VneuronRuleCommonDTO documentRule;
    private VneuronRuleCommonDTO clinicalTermsRule;
    private Object verificationResponse;
    private Object queryDocumentResponse;
}