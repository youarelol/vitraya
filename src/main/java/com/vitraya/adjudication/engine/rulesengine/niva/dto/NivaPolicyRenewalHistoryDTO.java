package com.vitraya.adjudication.engine.rulesengine.niva.dto;

import lombok.Data;

@Data
public class NivaPolicyRenewalHistoryDTO {
    private String Renewal_Id;
    private String policy_Number;
    private String Start_Date;
    private String End_Date;
    private String Policy_SI;
}
