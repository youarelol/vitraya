package com.vitraya.adjudication.engine.rulesengine.niva.dto;

import lombok.Data;

import java.util.Date;

@Data
public class ChronicIllnessDTO {
    private int illnessId;
    private Date diagnosisDate;
    private int numberOfMonths;
    private String illnessName;
    private String illnessIcdCode;
}
