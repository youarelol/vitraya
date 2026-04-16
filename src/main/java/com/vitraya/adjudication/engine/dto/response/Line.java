package com.vitraya.adjudication.engine.dto.response;

import lombok.Data;

@Data
public class Line{
	private String qc_confidence;
	private double confidence;
	private Coordinates coordinates;
	private String text;
}