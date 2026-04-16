package com.vitraya.adjudication.engine.dto.request;

import com.vitraya.adjudication.engine.dto.response.BillTariffResponseData;
import com.vitraya.adjudication.engine.dto.response.RiderDetails;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TariffRequest {
    private boolean success;
    private String bill_code;
    private String response_code;
    private BillTariffResponseData data;
    private String rerun;
    private List<RiderDetails> rider_details;
    private String insurer_code;
}
