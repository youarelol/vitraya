package com.vitraya.adjudication.engine.mysql.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

@Data
@Table("policy_product")
public class PolicyProduct {
    @Id
    private long id;

    @Column("insurance_agency_id")
    private long insuranceAgencyId;

    @Column("policy_name")
    private String policyName;

    @Column("product_code")
    private String productCode;

    @Column("active")
    private boolean active;

    @JsonIgnore
    @Column("deleted")
    private boolean deleted;

    @JsonIgnore
    @Column("date_created")
    private Date dateCreated;

    @JsonIgnore
    @Column("date_updated")
    private Date dateUpdated;
}
