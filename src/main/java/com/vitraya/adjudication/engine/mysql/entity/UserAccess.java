package com.vitraya.adjudication.engine.mysql.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

@Data
@Table(name = "user_access")
public class UserAccess {
    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    private int role;

    private int module;

    @Column("can_create")
    private Boolean canCreate;

    @Column("can_read")
    private Boolean canRead;

    @Column("can_update")
    private Boolean canUpdate;

    @Column("can_delete")
    private Boolean canDelete;

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
