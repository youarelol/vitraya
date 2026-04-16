package com.vitraya.adjudication.engine.mysql.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("claim_riders_details")
public class ClaimRiderDetails {

  @Id
  private long id;

  @Column("claim_data_id")
  private long claimDataId;

  @Column("intimation_number")
  private String intimationNumber;

  @Column("refill_flag_policy")
  private boolean refillFlagPolicy;

  @Column("reassure_benefit_amount")
  private boolean reassureBenefitAmount;

  @Column("policy_ported")
  private boolean policyPorted;

  @Column("safeguard")
  private boolean safeguard;

  @Column("safeguard_plus")
  private boolean safeguardPlus;

}
