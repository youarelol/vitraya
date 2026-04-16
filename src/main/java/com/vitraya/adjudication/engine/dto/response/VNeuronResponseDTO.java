package com.vitraya.adjudication.engine.dto.response;

import lombok.Data;

import java.util.HashMap;
import java.util.List;

@Data
public class VNeuronResponseDTO {
    private String id;
    private String referenceClaimId;
    private String createdOn;
    private String lastModifiedOn;
    private String createdBy;
    private String organization;
    private String lastModifiedBy;
    private String lastVerifiedOn;
    private String patientName;
    private List<String> procedureCodes;
    private String adjudicationResult;
    private String adjudicationReason;
    private String status;
    private String rerunStatus;
    private String claimNote;
    private String suspensionReason;
    private String hospitalCode;
    private String reportedMonth;
    private String scheme;
    private Patient patient;
    private String caseType;
    private HashMap<String, AdjudicationDetails> adjudicationDetails;
    private List<Object> procedureDetails;
    private List<DocumentsItem> documents;
    private List<Object> docGroup;
    private boolean deleteAfterAdjudication;
    private String hospitalType;
    private boolean annotated;
    private String annotationId;
    private String queryType;
    private List<InfoNotFoundItem> infoNotFound;
    private List<Object> docsNotFound;
    private String error_message;
    private String remarkCode;
    private List<DocumentsItem.DocumentDetails> document;

}