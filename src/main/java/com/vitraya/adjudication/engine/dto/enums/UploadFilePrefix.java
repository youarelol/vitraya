package com.vitraya.adjudication.engine.dto.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UploadFilePrefix {
    FINAL_BILL("FINAL_BILL", "Final Bill"),
    DISCHARGE_SUMMARY("DISCHARGE_SUMMARY", "Discharge Summary"),
    PHARMACY_BILL("PHARMACY_BILL", "Pharmacy Bill"),
    EXTERNAL_CLAIM_DOCUMENT("EXTERNAL_CLAIM_DOCUMENT", "External Claim Document"),
    OTHER_FILE("OTHER_FILE", "Other Document");

    private final String filePrefix;
    private final String documentType;
}
