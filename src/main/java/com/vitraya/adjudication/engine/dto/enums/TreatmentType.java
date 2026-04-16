package com.vitraya.adjudication.engine.dto.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TreatmentType {
    MEDICAL("Medical Management"),
    SURGICAL("Surgical Management");

    private final String value;

    public static TreatmentType getTreatmentType(String treatmentTypeStr) {
        for (TreatmentType treatmentType : values()) {
            if (treatmentType.getValue().equals(treatmentTypeStr)) {
                return treatmentType;
            }
        }
        return null;
    }
}
