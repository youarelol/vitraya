package com.vitraya.adjudication.engine.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EstimatesItem {
    private boolean showSummary;
    private boolean allowed;
    private BigDecimal amountFromTariff;
    private boolean coveredInPackage;
    private String ruleMessage;
    private String categoryName;
    private BigDecimal amountFromPackage;
    private int costCategory;
    private int numberOfUnits;
    private BigDecimal authorizedAmount;
    private BigDecimal mouDiscount;
    private boolean defaultRender;
    private BigDecimal estimateAmount;
    private boolean considerInTotalStay;
    private String masterCategoryName;
    private boolean isIrdaiNonPayable;
}