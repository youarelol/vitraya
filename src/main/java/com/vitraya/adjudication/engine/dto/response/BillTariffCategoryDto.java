package com.vitraya.adjudication.engine.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BillTariffCategoryDto {
    private String name;
    private List<LineItemsItem> lineItems;

}
