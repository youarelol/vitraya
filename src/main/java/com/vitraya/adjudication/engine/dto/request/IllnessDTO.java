package com.vitraya.adjudication.engine.dto.request;

import lombok.Data;

@Data
public class IllnessDTO {
	private long id;
	private String defaultICDCode;
	private String illnessCode;
	private String illnessName;

	public IllnessDTO updateIllness(long id, String defaultICDCode, String illnessCode, String illnessName) {
		this.id = id;
		this.defaultICDCode = defaultICDCode;
		this.illnessCode = illnessCode;
		this.illnessName = illnessName;
		return this;
	}
}