package com.vitraya.adjudication.engine.mysql;

import com.vitraya.adjudication.engine.dto.IllnessResultDTO;

import java.util.List;

public interface IllnessesRepositoryCustom {
        List<IllnessResultDTO> findByProcedureId(String procedureId);
}
