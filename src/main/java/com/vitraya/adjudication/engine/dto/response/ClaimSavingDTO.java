package com.vitraya.adjudication.engine.dto.response;

import lombok.Data;

import java.util.HashMap;

@Data
public class ClaimSavingDTO {
    private BillSavingsDTO billTariffSaving;
    private HashMap<String, BillSavingsDTO> categorySavingMap;
}
