package com.vitraya.adjudication.engine.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HospitalServiceTypeDTO {
	private BigDecimal roomTariffPerDay;
	private String serviceCode;
	private String roomType;
}