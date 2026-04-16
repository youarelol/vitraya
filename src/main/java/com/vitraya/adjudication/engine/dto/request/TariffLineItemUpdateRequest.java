package com.vitraya.adjudication.engine.dto.request;

import com.vitraya.adjudication.engine.dto.response.TariffLineItemResponseDTO;
import lombok.Data;

import java.util.List;

@Data
public class TariffLineItemUpdateRequest {
    private String claimDataIdStr;
    private long claimDataId;
    private String claimNumber;
    private String insurerUserId;
    private List<TariffLineItemResponseDTO> editedLineItems;
    private List<TariffLineItemResponseDTO> newLineItems;
    private List<TariffLineItemResponseDTO> deletedLineItems;
}
