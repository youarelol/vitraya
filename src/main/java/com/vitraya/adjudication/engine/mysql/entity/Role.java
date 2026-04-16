package com.vitraya.adjudication.engine.mysql.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

@Data
@Table(name = "roles")
public class Role {
    @Id
    private Long id;

    @Column("role_name")
    private String roleName;

    @Column
    private Boolean status;

    @Column("organisation_id")
    private Long organisationId;

    // Audit columns
    @Column("created_by")
    private Long createdBy;

    @Column("updated_by")
    private Long updatedBy;

    @Column("date_created")
    private Date createdOn;

    @Column("date_updated")
    private Date updatedOn;
}
