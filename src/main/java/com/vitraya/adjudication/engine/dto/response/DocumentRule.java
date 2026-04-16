package com.vitraya.adjudication.engine.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class DocumentRule{
	private String booleanOperator;
	private List<StatementsItem> statements;
	private boolean adjudicationResult;
}