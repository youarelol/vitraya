package com.vitraya.adjudication.engine.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ClaimHistory{
	private boolean restorationFlag;
	private List<ClaimHistoriesItem> claimHistories;
	private BigDecimal totalSIUtilised;
}