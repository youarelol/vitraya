package com.vitraya.adjudication.engine.dto.request;

import com.vitraya.adjudication.engine.mysql.entity.NivaRequestData;
import lombok.Data;

@Data
public class A2SCallBackRequest {
    private boolean success;
    private String message;
    private String callback_stage; // "PREAUTH | DUPLICATE_PREAUTH | INTERIM | DISCHARGE | QUERY_REPLY | SETTLEMENT"
    private String preauth_id;
    private String intimation_number;
    private String requestData;
}
