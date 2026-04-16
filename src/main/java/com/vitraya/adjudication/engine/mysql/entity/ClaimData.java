package com.vitraya.adjudication.engine.mysql.entity;

import com.vitraya.adjudication.engine.dto.enums.*;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@Table("claim_data")
public class ClaimData {
    @Id
    private long id;

    @Column("intimation_number")
    private String intimationNumber;

    @Column("insurance_agency_id")
    private long insuranceAgencyId;

    @Column("tpa_id")
    private long tpaId;

    @Column("hospital_id")
    private long hospitalId;

    @Column("claim_type")
    private ClaimType claimType;

    @Column("patient_name")
    private String patientName;

    @Column("patient_age")
    private int patientAge;

    @Column("designation")
    private String designation;

    @Column("current_policy_end_date")
    private Date currentPolicyEndDate;

    @Column("current_policy_start_date")
    private Date currentPolicyStartDate;

    @Column("reason_for_hospitalization")
    private String reasonForHospitalization;

    @Column("date_of_birth")
    private Date dateOfBirth;

    @Column("cover_code")
    private String coverCode;

    @Column("hospital_zone")
    private Zone hospitalZone;

    @Column("status")
    private EnhancementStatus status;

    @Column("base_sum_insured")
    private BigDecimal baseSumInsured;

    @Column("remaining_sum_insured")
    private BigDecimal remainingSumInsured;

    @Column("current_policy_inception_date")
    private Date currentPolicyInceptionDate;

    @Column("is_enhancement_processed")
    private boolean isEnhancementProcessed;

    @Column("active")
    private boolean active;

    @Column("deleted")
    private boolean deleted;

    @Column("date_created")
    private Date dateCreated;

    @Column("date_updated")
    private Date dateUpdated;

    @Column("date_of_admission")
    private Date dateOfAdmission;

    @Column("date_of_discharge")
    private Date dateOfDischarge;

    @Column("icd_code")
    private String icdCode;

    @Column("policy_renewal_history")
    private String policyRenewalHistory;

    @Column("procedure_id")
    private long procedureId;

    @Column("product_code")
    private String productCode;

    @Column("room_type")
    private String roomType;

    @Column("copay_zone")
    private Zone copayZone;

    @Column("policy_variant")
    private String policyVariant;

    @Column("treatment_type")
    private TreatmentType treatmentType;

    @Column("assigned")
    private boolean assigned;

    @Column("assigned_by")
    private String assigned_by;

    @Column("is_insurer_visible")
    private boolean isInsurerVisible;

    @Column("pushed_to_insurer")
    private boolean pushedToInsurer;

    @Column("claim_history")
    private String claimHistory;

    @Column("policy_number")
    private String policyNumber;

    @Column("claim_status")
    private ClaimStatus claimStatus;

    @Column("attendant_mobile_number")
    private String attendantMobileNumber;

    @Column("diagnosis")
    private String diagnosis;

    @Column("ped_list")
    private String pedList;

    @Column("date_of_first_diagnosis")
    private Date dateOfFirstDiagnosis;

    @Column("adjudication_status")
    private String adjudicationStatus;

    @Column("medical_card_number")
    private String medicalCardNumber;

    @Column("insurer_identifier")
    private String insurerIdentifier;

    @Column("in_scope_hospital")
    private boolean inScopeHospital;

    @Column("in_scope_policy")
    private boolean inScopePolicy;

    @Column("in_scope_procedure")
    private boolean inScopeProcedure;

    @Column("member_no")
    private String memberNo;

    @Column("initial_tat")
    private int initialTat;

    @Column("discharge_tat")
    private int dischargeTat;

    @Column("claim_rerun")
    private boolean claimRerun;

    @Column("claim_status_text")
    private String claimStatusText;

    @Column("discharge_received_time")
    private Date dischargeReceivedTime;

    @Column("is_email_flow")
    private boolean isEmailFlow;

    @Column("txn_id")
    private String txnId;

    @Column("preauth_bill_present_flag")
    private boolean preauthBillPresentFlag;

    @Column("doctor_detail_for_settlement")
    private String doctorDetailForSettlement;

    @Column("claim_number")
    private String claimNumber;

    @Column("received_reversefeed")
    private boolean receivedReverseFeed;

    @Column("line_of_treatment_details")
    private String lineOfTreatmentDetails;
}