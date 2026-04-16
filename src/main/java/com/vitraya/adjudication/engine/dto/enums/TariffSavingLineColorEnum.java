package com.vitraya.adjudication.engine.dto.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TariffSavingLineColorEnum {
    FULL_NON_PAYABLE_COLOR("#fff8de"),
    PC_NON_PAYABLE_COLOR("#c9bfe3"),
    SAVINGS_COLOR("#f0fff0"),
    NO_SAVINGS_COLOR("#ffffff"),
    NEW_LINE_ITEM_ADDED_COLOR("#ffe4e1");

    private final String colorCode;
}
