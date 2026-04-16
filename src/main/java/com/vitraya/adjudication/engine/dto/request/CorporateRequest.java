package com.vitraya.adjudication.engine.dto.request;

import com.vitraya.adjudication.engine.dto.enums.CorporateStatusEnum;
import com.vitraya.adjudication.engine.dto.enums.CorporateTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CorporateRequest {
    @NotNull(message = "{validation.error.corporate.name.required}")
    private String name;

    @NotNull(message = "{validation.error.corporate.code.required}")
    private String corporateCode;

    private CorporateStatusEnum status;
    private CorporateTypeEnum type;
    private String bill_parsed_config;
    private String enabled_for_review;
}
