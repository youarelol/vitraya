package com.vitraya.adjudication.engine.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BillAmounts{
	private BigDecimal total_bill_amount;
	private BigDecimal difference_in_amount;
	private BigDecimal amount_match_percentage;
	private BigDecimal total_sum_of_line_items;
	private BigDecimal difference_in_amount_orignal;
	private BigDecimal amount_match_percentage_orignal;
	private BigDecimal total_sum_of_line_items_orignal;
}