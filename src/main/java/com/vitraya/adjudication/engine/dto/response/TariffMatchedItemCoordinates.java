package com.vitraya.adjudication.engine.dto.response;

import lombok.Data;

@Data
public class TariffMatchedItemCoordinates{
	private int page_number;
	private String top;
	private String left;
	private String bottom;
	private String right;
}