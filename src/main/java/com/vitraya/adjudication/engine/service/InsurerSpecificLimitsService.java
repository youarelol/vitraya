package com.vitraya.adjudication.engine.service;

import java.math.BigDecimal;

public interface InsurerSpecificLimitsService {

    BigDecimal getUpperLimit(String procedure_code,String hospital_code);

}
