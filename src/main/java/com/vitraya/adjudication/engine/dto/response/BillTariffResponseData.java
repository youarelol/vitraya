package com.vitraya.adjudication.engine.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BillTariffResponseData {
    private Metadata metadata;
    private List<LineItemsItem> line_items;
    private BillAmounts bill_amounts;
    private TariffAmounts tariff_amounts;
    private List<CategorySummaryItem> category_summary;
    private AmountsAfterPayables amounts_after_payables;
}