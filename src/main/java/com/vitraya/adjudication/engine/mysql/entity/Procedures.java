package com.vitraya.adjudication.engine.mysql.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vitraya.adjudication.engine.dto.enums.TreatmentType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

@Data
@Table(name = "procedures")
public class Procedures {
    @Id
    private long id;

    @Column("name")
    private String name;

    @Column("treatment_type")
    private TreatmentType treatmentType;

    @Column("procedure_code")
    private String procedureCode;

    @Column("vneuron_sctid_code")
    private String vneuronSctidCode;

    @Column("pcs_code")
    private String pcsCode;

    @Column("benefit_code")
    private String benefitCode;

    @Column("active")
    private boolean active;

    @Column("deleted")
    private boolean deleted;

    @JsonIgnore
    @Column("date_created")
    private Date createTime;

    @JsonIgnore
    @Column("date_updated")
    private Date updateTime;
}
