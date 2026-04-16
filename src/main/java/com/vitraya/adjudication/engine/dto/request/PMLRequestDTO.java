package com.vitraya.adjudication.engine.dto.request;

import com.vitraya.adjudication.engine.dto.enums.Zone;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class PMLRequestDTO {
    private String claimNumber;
    private String product_code;
    private String product_name;
    private String uin_number;
    private String policy_version;
    private String policy_subplan;
    private String policy_plan;
    private BigDecimal total_claim_amount;
    private String procedure_name;
    private String snomed_code;
    private BigDecimal policy_base_sum_insured;
    private BigDecimal policy_available_sum_insured;
    private String policy_inception_date;
    private String date_of_admission;
    private String date_of_discharge;
    private String date_of_first_diagnosis;
    private String policy_start_date;
    private String policy_end_date;
    private String reason_for_hospitalization;
    private BigDecimal tariff_room_rent;
    private String date_of_birth;
    private int actual_age;
    private int age_at_inception;
    private Zone copay_zone;
    private Zone hospital_zone;
    private List<PolicyRenewalHistoryDTO> policy_renewal;
    private List<BenefitDTO> benefit_groups;
    private String claim_room_type;
    private String hospital_code;
    private String claim_stage;
    private String treatment_type;
    private List<PedDTO> pedList;
    private long claimDataId;
    private String singlePrivateACRoomName;
    private BigDecimal singlePrivateACTariffRate;
    private BigDecimal optedRoomTariffRate;
    private String relationship;
    private String category;
}
