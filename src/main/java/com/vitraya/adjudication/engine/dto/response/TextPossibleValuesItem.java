package com.vitraya.adjudication.engine.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class TextPossibleValuesItem{
	private String textValue;
	private List<String> textValueAlias;
}