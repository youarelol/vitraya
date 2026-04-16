package com.vitraya.adjudication.engine.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class OverridesItem{
	private List<Object> hospitals;
	private List<String> excludeDocumentHeaders;
	private List<String> procedures;
	private List<String> overrideValue;
}