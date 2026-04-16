package com.vitraya.adjudication.engine.dto.response;


import com.vitraya.adjudication.engine.mysql.entity.BillLineItemSaving;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LineItemSavingSegregationDto {
    private List<LineItemsItemWithStageDto> added;
    private List<BillLineItemSaving> deleted;
    private List<LineItemsItemWithStageDto> edited;
    private List<LineItemsItemWithStageDto> nonChanged;



}