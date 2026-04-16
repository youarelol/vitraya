package com.vitraya.adjudication.engine.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class ClinicalTermsRule{
	private String adjudicationResult;
	private String booleanOperator;
	private Object statements;
}