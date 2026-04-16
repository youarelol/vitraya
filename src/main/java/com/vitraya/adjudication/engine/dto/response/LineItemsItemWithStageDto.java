package com.vitraya.adjudication.engine.dto.response;


import lombok.Data;

@Data
public class LineItemsItemWithStageDto {

    private LineItemMetadata metadata;
    private LineItemData data;
    private Boolean newItem;
    private Boolean edited;
    private Boolean deleted;


    public static LineItemsItemWithStageDto from(LineItemsItem source) {
        LineItemsItemWithStageDto item = new LineItemsItemWithStageDto();
        item.setMetadata(source.getMetadata());
        item.setData(source.getData());

        return item;
    }

}
