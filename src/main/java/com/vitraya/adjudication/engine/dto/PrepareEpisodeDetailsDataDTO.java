package com.vitraya.adjudication.engine.dto;

import com.vitraya.adjudication.engine.dto.response.BillTariffPmlDto;
import com.vitraya.adjudication.engine.mysql.entity.ClaimAdjudicationResult;
import com.vitraya.adjudication.engine.mysql.entity.ClaimData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrepareEpisodeDetailsDataDTO {
    private boolean packageCase;
    private BillTariffPmlDto billTariffPmlDto;
    private long hospitalId;
    private String bedType;
    private String benefitType;
    private ClaimAdjudicationResult claimAdjudicationResult;
    private Date admissionDate;
    private Date dischargeDate;
    private ClaimData claimData;
    private boolean rejected;
    private boolean preAuth;
    private boolean discharge;
    private boolean interim;
    private boolean query;
    private boolean reconsideration;
    private boolean defaultTariffApplied;
    private String managementType;
}
