package com.vitraya.adjudication.engine.dto.enums;

import lombok.Getter;

@Getter
public enum VitrayaErrorCodes {
    INVALID_REQUEST("VT00E-1000", "error.invalid.request"),
    INVALID_CORPORATE_CODE("VT00E-1001", "error.invalid.corporate.code"),
    INVALID_CORPORATE_NAME("VT00E-1002", "error.invalid.corporate.name"),
    INVALID_CORPORATE_ID("VT00E-1003", "error.invalid.corporate.id"),
    INVALID_USER_REQUEST("VT00E-1004", "error.invalid.user.request"),
    INVALID_CLAIM_DATA_REQUEST("VT00E-1005", "error.invalid.claim.data.request"),
    CLAIM_DOCUMENT_NOT_FOUND("VT00E-1006", "error.claim.document.not.found"),
    INVALID_CLAIM_DATA_REQUEST_DOCUMENT_EMPTY("VT00E-1007", "error.invalid.claim.data.request.document.empty"),
    EXCEPTION_PARSE_CLAIM_DATA("VT00E-1008", "exception.parse.claim.request"),
    INVALID_CLAIM_DATA_ID("VT00E-1009", "error.invalid.claim.data.id"),
    INVALID_CLAIM_DATA_REQUEST_CLAIM_EMPTY("VT00E-1009", "error.invalid.claim.data.request.claim.empty"),
    DUPLICATE_CLAIM_REQUEST("VT00E-1010", "duplicate.claim.request"),
    BILL_TARIFF_RESPONSE_NOT_FOUND("VT00E-1011", "bill.tariff.response.not.found"),
    ERROR_OCCURED_SAVING_CLAIM_STATS("VT00E-1012", "error.occurred.saving.claim.stats"),
    INVALID_BILL_TARIFF_RESPONSE_RECEIVED("VT00E-1013", "invalid.bill.tariff.response.received"),
    ERROR_OCCURED_PROCESSING_BILL_TARIFF_REQUEST("VT00E-1014", "error.occurred.processing.bill.tariff.request"),
    INVALID_CLAIM_MODULE_FOUND("VT00E-1015", "invalid.claim.module.found"),
    CLAIM_MODULE_NOT_FOUND("VT00E-1016", "claim.module.not.found"),
    CLAIM_UNDER_PROCESSING("VT00E-1017", "claim.under.processing"),
    IFRAME_INVALID_REQUEST("VT00E-1018", "iframe.invalid.request"),
    INVALID_CLAIM("VT00E-1019", "claim.invalid"),
    EMPTY_CREATE_CLAIM_REQUEST_RECEIVED("VT00E-1020", "empty.create.claim.request.received"),
    ENHANCEMENT_CONFIG_NOT_FOUND("VT00E-1021", "enhancement.config.not.found"),
    INVALID_STATUS_RECEIVED("VT00E-1022", "invalid.status.found"),
    ERROR_INVALID_CREDENTIALS("VT00E-1023", "error.invalid.credentials"),
    ERROR_UPDATING_LINE_ITEMS("VT00E-1024", "error.updating.line.items"),
    CLAIM_DECISION_NOT_UPDATED("VT00E-1025", "error.claim.decision.not.updated"),
    CLAIM_DECISION_NOT_PUSHED("VT00E-1026", "error.claim.decision.not.pushed"),
    INVALID_TARIFF_LINE_ITEM_ADD_UPDATE_REQUEST("VT00E-1027", "error.invalid.tariff.line.item.add.update.request"),
    ERROR_USER_BLOCKED("VT00E-1028", "error.user.blocked"),
    INVALID_OTP_VERIFICATION_REQUEST("VT00E-1029", "invalid.otp.verification.request"),
    INVALID_OTP_RECEIVED("VT00E-1030", "invalid.otp"),
    INCORRECT_OTP_RECEIVED("VT00E-1031", "invalid.otp.received"),
    ERROR_OCCURRED_PROCESSING_TARIFF_REQUEST("VT00E-1032", "error.occurred.processing.tariff.request"),
    PROCEDURE_HAS_NO_ILLNESS("VT00E-1033", "procedure.has.no.illness"),
    NO_HOSPITAL_FOUND("VT00E-1034", "no.hospital.found"),
    HOSPITAL_HAS_NO_ROOM("VT00E-1035", "hospital.has.no.room.type"),
    NO_POLICY_FOUND("VT00E-1036", "no.policy.found"),
    NO_PROCEDURE_FOUND("VT00E-1037", "no.procedure.found"),
    INVALID_TOKEN("VT00E-1038","invalid.token"),
    NO_TOKEN("VT00E-1039","no.token"),
    OTP_LIMIT_EXCEEDED("VT00E-1040","OTP Limit Exceeded"),
    PML_DATA_NOT_FOUND("VT00E-1041", "pml.data.not.found");

    private final String code;
    private final String messageKey;

    VitrayaErrorCodes(String code, String messageKey) {
        this.code = code;
        this.messageKey = messageKey;
    }

    public static String getMessageKey(String code) {
        for (VitrayaErrorCodes errorCode : values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode.getMessageKey();
            }
        }
        return "error.unknown";
    }
}
