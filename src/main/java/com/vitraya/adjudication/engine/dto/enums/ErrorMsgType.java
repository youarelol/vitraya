package com.vitraya.adjudication.engine.dto.enums;

public enum ErrorMsgType {

    MANUAL("MANUAL","Manual",
            "Manual, Kindly process it manually"),

    SEND_VIA_EMAIL_CHANNEL("VITRAYA SENT CLAIM ON EMAIL","Sent via Email Channel",
            "Sent via Email Channel, Kindly process it manually"),

    OUT_OF_SCOPE_PROCEDURE("OUT OF SCOPE PROCEDURE","Out of scope procedure",
            "Out of scope procedure, Kindly process it manually"),

    OUT_OF_SCOPE_POLICY("OUT OF SCOPE POLICY","Out of scope policy",
            "Out of scope Policy, Kindly process it manually"),

    MEDICAL_ADMISSIBILITY_FAILURE("VNEURON","Medical Admissibility engine failure",
            "Medical Admissibility engine fails to run, Kindly process it manually"),

    PML_ENGINE_FAILURE("PML","PML engine failure",
            "PML engine failed to run, Kindly process it manually"),

    BILL_TARIFF_ENGINE_FAILURE("Bill Tariff","Bill Tariff engine failure",
            "Bill-tariff engine failed to run, Kindly process it manually"),

    NIVA_PREAUTH_CREATION_API_FAILURE("NIVA PREAUTH CREATION","Niva preauth creation api failure",
            "Niva preauth creation api fails to run, Kindly process it manually"),

    NIVA_INTERIM_API_FAILURE("NIVA INTERIM","Niva interim api failure",
            "Niva interim api fails to run, Kindly process it manually"),

    NIVA_DISCHARGE_API_FAILURE("NIVA DISCHARGE","Niva discharge api failure",
            "Niva discharge api fails to run, Kindly process it manually"),

    NIVA_QUERY_REPLY_API_FAILURE("NIVA QUERY REPLY","Niva query reply api failure",
            "Niva query reply api fails to run, Kindly process it manually"),

    NIVA_RECONSIDER_API_FAILURE("NIVA RECONSIDER","Niva reconsider api failure",
            "Niva reconsider api fails to run, Kindly process it manually"),

    NIVA_SOAP_API_FAILURE("NIVA SOAP API","Niva SOAP API failure",
            "Niva SOAP API fails to respond, Kindly process it manually"),

    NIVA_SETTLEMENT_API_FAILURE("NIVA SETTLEMENT API","Niva Settlement API failure",
            "Niva Settlement API fails to respond, Kindly process it manually");



    private final String failureEngine;
    private final String reason;
    private final String message;

    ErrorMsgType(String failureEngine,String reason, String message) {
        this.failureEngine = failureEngine;
        this.reason = reason;
        this.message = message;
    }

    public String getFailureEngine() {
        return failureEngine;
    }

    public String getReason() {
        return reason;
    }

    public String getMessage() {
        return message;
    }
}
