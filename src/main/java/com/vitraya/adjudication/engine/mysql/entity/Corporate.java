package com.vitraya.adjudication.engine.mysql.entity;

import com.vitraya.adjudication.engine.dto.enums.ClaimDataParseMapping;
import com.vitraya.adjudication.engine.dto.enums.CorporateStatusEnum;
import com.vitraya.adjudication.engine.dto.enums.CorporateTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

@Data
@Table(name = "corporates")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Corporate {
    @Id
    private Long id;

    @Column
    private String name;

    @Column("corporate_code")
    private String corporateCode;
    private CorporateStatusEnum status;
    private CorporateTypeEnum type;

    @Column("claim_data_parse_mapping")
    private ClaimDataParseMapping claimDataParseMapping;

    @Column("enabled_for_review")
    private boolean enabledForReview;
    private String created_by;
    private Date date_created;
    private String updated_by;
    private Date date_updated;
}
