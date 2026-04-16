package com.vitraya.adjudication.engine.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class ValuesToFindItem{
	private String id;
	private String name;
	private List<String> alias;
	private String ICD_code;
	private PEDConfig PED_config;
	private boolean PED_flag;
}