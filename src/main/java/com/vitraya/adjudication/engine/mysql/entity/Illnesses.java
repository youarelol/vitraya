package com.vitraya.adjudication.engine.mysql.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;
import java.util.List;

@Data
@Table(name = "illnesses")
public class Illnesses {
    @Id
    private long id;

    @Column("category")
    private String illnessCategory;

    @Column("name")
    private String illnessName;

    @Column("default_icd_code")
    private String defaultICDCode;

    @Column("related_disease")
    private String relatedDisease;

    @Column("relevant_procedures")
    private String procedures;

    @Column("illness_code")
    private String illnessCode;

    @Column("active")
    private boolean active;

    @Column("deleted")
    private boolean deleted;

    @Column("date_created")
    private Date dateCreated;

    @Column("date_updated")
    private Date dateUpdated;

    @Transient
    private List<String> relatedDiseaseList;

    @Transient
    private List<Integer> relevantProcedures;

    @Transient
    private List<String> relevantProceduresCode;
}
