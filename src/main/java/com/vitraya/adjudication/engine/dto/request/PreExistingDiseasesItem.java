package com.vitraya.adjudication.engine.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class PreExistingDiseasesItem {
    private List<PedListItem> pedList;
    private boolean pedBuyBack;

    public List<PedListItem> getPedList() {
        return pedList;
    }

    public boolean isPedBuyBack() {
        return pedBuyBack;
    }
}