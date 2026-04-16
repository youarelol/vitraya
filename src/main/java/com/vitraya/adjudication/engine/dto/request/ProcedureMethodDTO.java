package com.vitraya.adjudication.engine.dto.request;

import lombok.Data;

@Data
public class ProcedureMethodDTO {
	private String pCMCode;
	private String procedureCode;
	private String procedureMethodName;
	private String procedureMethodCode;
}