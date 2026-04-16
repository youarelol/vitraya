package com.vitraya.adjudication.engine.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class LineItemMetadata {
    private Line line;
    private List<WordsItem> words;
}