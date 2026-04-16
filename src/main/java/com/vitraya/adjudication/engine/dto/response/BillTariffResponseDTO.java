package com.vitraya.adjudication.engine.dto.response;


import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BillTariffResponseDTO {
    private boolean success;
    private String bill_code;
    private String insurer_code;
    private String response_code;
    private BillTariffResponseData data;
    private String message;
    private String unique_identifier;
    private String parser_type;
    private String failed_remarks;
    private String rerun;
    private List<RiderDetails> riders_details;
}
