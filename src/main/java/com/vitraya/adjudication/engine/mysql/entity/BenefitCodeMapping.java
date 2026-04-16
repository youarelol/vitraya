package com.vitraya.adjudication.engine.mysql.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

@Table("benefit_code_mapping")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class BenefitCodeMapping {
    @Id
    private long id;

    @Column("category")
    private String category;

    @Column("benefit_code")
    private String benefitCode;

    @Column("expense_master_category")
    private String expenseMasterCategory;

    @Column("date_created")
    private Date dateCreated;

    @Column("date_updated")
    private Date dateUpdated;
}
