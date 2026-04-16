package com.vitraya.adjudication.engine.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class FkClinicalUnitItem{
	private String name;
	private List<AliasItem> alias;
}