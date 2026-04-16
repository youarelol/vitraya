package com.vitraya.adjudication.engine.dto.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DocumentStatusEnum {
    DOCUMENT_DELETED(0),
    DOCUMENT_ADDED(1),
    DOCUMENT_UPDATED(2),
    DOCUMENT_SHARED(3);

    private final int state;

    public static DocumentStatusEnum find(Integer documentStatus) {
        for (DocumentStatusEnum documentStatusEnum : DocumentStatusEnum.values()) {
            if (documentStatusEnum.state == documentStatus) {
                return documentStatusEnum;
            }
        }
        return null;
    }
}
