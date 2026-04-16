package com.vitraya.adjudication.engine.dto.response;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LineItemData {
    private ValueConfidenceDTO rate;
    private ValueConfidenceDTO amount;
    private int row_id;
    private Tariff tariff;
    private ValueConfidenceDTO quantity;
    private ValueConfidenceDTO description;
    private ValueConfidenceDTO category_from_document;
    private VitrayaMasterCategory vitraya_master_category;
}