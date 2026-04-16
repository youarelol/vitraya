package com.vitraya.adjudication.engine.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VitrayaMasterCategory {
    private int confidence;
    private String level2Category;
    private String value;

    public VitrayaMasterCategory(String billLineItem, int i, String level2Category) {
        this.level2Category = level2Category;
        this.value = billLineItem;
        this.confidence = i;
    }
}