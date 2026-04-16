package com.vitraya.adjudication.engine.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorySummaryItem{
	private String category_name;
	private BigDecimal requested_amount;
	private BigDecimal admissible_amount;
	private BigDecimal admissible_amount_without_procedure_construct;
	private BigDecimal amount_for_irdai_payable;
	private BigDecimal amount_after_procedure_construct;
	private BigDecimal deductions;
	private List<LineItemsItem> line_items;
}