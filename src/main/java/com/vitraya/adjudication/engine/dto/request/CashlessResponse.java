package com.vitraya.adjudication.engine.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CashlessResponse {
    private String claimNumber;
    @JsonProperty(value = "status")
    private String claimStatusInString;
    @JsonProperty(value = "remarks")
    private String query;
    @JsonProperty(value = "Files")
    private List<FilesObj> files;
    private BigDecimal approvedAmount;
    @JsonProperty(value = "COI")
    private String coi;
    private String membershipId;
    private String sourceSystem;
    private String customerType;
    private String preAuthId;


    @Data
    public static class FilesObj {
        private String name;
        private String docId;
    }
}
