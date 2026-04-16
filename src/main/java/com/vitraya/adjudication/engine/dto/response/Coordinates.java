package com.vitraya.adjudication.engine.dto.response;

import lombok.Data;

@Data
public class Coordinates{
	private int pageNumber;
	private String top;
	private String left;
	private String bottom;
	private String right;
}