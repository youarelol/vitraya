package com.vitraya.adjudication.engine.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class PEDConfig{
	private List<Object> defaultInclusionList;
	private List<Object> defaultExclusionList;
}