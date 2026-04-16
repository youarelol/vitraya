package com.vitraya.adjudication.engine.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class ValueToCompare{
	private String valueType;
	private NumberRange numberRange;
	private boolean tabular;
	private List<OverridesItem> overrides;
	private List<FkClinicalUnitItem> fkClinicalUnit;
	private boolean distributedAttributeValue;
	private Object textPossibleValues;
	// private List<PatternOverridesForValuesItem> patternOverridesForValues;
	private List<AuditTrailItem> auditTrail;
	private String name;
	private List<String> alias;
	private String id;
	private List<Object> overrideMessage;
}