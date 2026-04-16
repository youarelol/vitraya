package com.vitraya.adjudication.engine.mysql.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("service_type")
public class ServiceType {
    @Id
    @Column("id")
    private long id;

    @Column("service_type")
    private String serviceType;

    @Column("service_type_description")
    private String serviceTypeDescription;

    @Column("benefit_type")
    private String benefitType;

    @Column("benefit_head")
    private String benefitHead;

    @Column("vitraya_master")
    private String vitrayaMaster;

    @Column("is_enabled")
    private boolean isEnabled;
}
