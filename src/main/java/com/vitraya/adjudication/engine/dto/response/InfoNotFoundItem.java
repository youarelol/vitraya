package com.vitraya.adjudication.engine.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class InfoNotFoundItem{
	private List<String> evidence;
	private List<String> procedure;
	private List<String> clinicalAttribute;
	private List<String> diagnosis;
}