package com.vitraya.adjudication.engine.dto.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AdjudicationStatus {
    PENDING("Pending", "Pending", "U"),
    PRE_AUTH_APPROVED("Initial Authorized", "AP", "A"),
    INTERIM_APPROVED("Interim Authorized", "AP", "A"),
    DISCHARGE_APPROVED("Discharge Authorized", "AP", "A"),
    REJECTED("REJECTED", "DN", "A"),
    PRE_AUTH_QUERY("Query Raised-Initial Pre-Authorzation", "QR", "A"),
    INTERIM_QUERY("Query Raised-Interim Pre-Authorzation", "QR", "A"),
    DISCHARGE_QUERY("Query Raised-Discharge Pre-Authorzation", "QR", "A"),
    OUT_OF_SCOPE_POLICY("Out of scope policy", "RMV", "O"),
    OUT_OF_SCOPE_PROCEDURE("Out of scope procedure", "RMV", "O"),
    OUT_OF_SCOPE_HOSPITAL("Out of scope hospital", "RMV", "O"),
    RELEVANT_DOCUMENT_NOT_FOUND("Relevant document not found", "FL", "F"),
    FAILED("FAILED", "FL", "F"),
    APPROVED("APPROVED", "AP", "A"),
    MANUAL("MANUAL", "M", "M"),
    QUERY("QUERY", "QR", "A"),
    ACCEPTED("ACCEPTED", "AP", "A");

    private final String adjudicationStatus;
    private final String insurerStatus;
    private final String adjudicationDecision;
}
