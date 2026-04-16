package com.vitraya.adjudication.engine.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class AuditTrailItem{
	private String lastModifiedOn;
	private List<String> overrideValue;
	private String lastModifiedBy;
}