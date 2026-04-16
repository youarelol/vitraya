package com.vitraya.adjudication.engine.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PMLSavingsDTO {

    private BigDecimal mouSavings = BigDecimal.ZERO;
    private BigDecimal copaySavings = BigDecimal.ZERO;
    private BigDecimal sublimitSavings = BigDecimal.ZERO;
    private BigDecimal proportionalSavings = BigDecimal.ZERO;


}
