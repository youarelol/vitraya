package com.vitraya.adjudication.engine.dto.request;

import lombok.Data;

@Data
public class ProcedureDTO {
	private long id;
	private String snowmedCode;
	private String procedureCode;
	private String name;

	public ProcedureDTO updateProcedure(long id, String vneuronSctidCode, String name, String procedureCode) {
		this.id = id;
		this.snowmedCode = vneuronSctidCode;
		this.name = name;
		this.procedureCode = procedureCode;

		return this;
	}
}