package com.vitraya.adjudication.engine.mysql.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

@Data
@Table("enhancement_config")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnhancementConfig {
    @Id
    private long id;

    @Column("hospital_id")
    private long hospitalId;

    @Column("insurance_agency_id")
    private long insuranceAgencyId;

    @Column("is_pml_enabled")
    private boolean isPmlEnabled;

    @Column("is_bill_tariff_enabled")
    private boolean isBillTariffEnabled;

    @Column("is_vneuron_enabled")
    private boolean isVneuronEnabled;

    @Column("is_qc_enabled")
    private boolean isQCEnabled;

    @Column("active")
    private boolean active;

    @Column("deleted")
    private boolean deleted;

    @Column("date_created")
    private Date dateCreated;

    @Column("date_updated")
    private Date dateUpdated;
}
