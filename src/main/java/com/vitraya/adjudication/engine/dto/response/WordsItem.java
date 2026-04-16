package com.vitraya.adjudication.engine.dto.response;

import lombok.Data;

@Data
public class WordsItem{
	private double confidence;
	private Coordinates coordinates;
	private String text;
}