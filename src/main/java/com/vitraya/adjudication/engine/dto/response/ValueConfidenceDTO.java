package com.vitraya.adjudication.engine.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ValueConfidenceDTO {
    private double confidence;
    private String value;

    public ValueConfidenceDTO(String billLineItem, int i) {
        this.value = billLineItem;
        this.confidence = i;
    }
}