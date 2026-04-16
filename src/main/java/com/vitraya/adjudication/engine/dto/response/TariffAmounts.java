package com.vitraya.adjudication.engine.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TariffAmounts{
	private BigDecimal total_admissible_amount;
	private BigDecimal deduction;
	private BigDecimal total_admissible_amount_without_procedure_construct;
	private BigDecimal deduction_without_procedure_construct;
}