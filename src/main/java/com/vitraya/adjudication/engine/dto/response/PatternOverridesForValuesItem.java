package com.vitraya.adjudication.engine.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class PatternOverridesForValuesItem{
	private List<String> hospitals;
	private List<List<OverrideItemItem>> override;
}