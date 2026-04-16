package com.vitraya.adjudication.engine.service;

import com.vitraya.adjudication.engine.dto.IllnessResultDTO;
import com.vitraya.adjudication.engine.dto.request.IllnessDTO;
import com.vitraya.adjudication.engine.dto.request.VitrayaInsurerClaimDataDTO;
import com.vitraya.adjudication.engine.mysql.entity.Illnesses;
import com.vitraya.adjudication.engine.mysql.repository.IllnessesRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IllnessService {

    private final IllnessesRepository illnessesRepository;

    public IllnessService(IllnessesRepository illnessesRepository) {
        this.illnessesRepository = illnessesRepository;
    }

    public IllnessDTO getIllness(VitrayaInsurerClaimDataDTO vitrayaInsurerClaimData) {
        Illnesses illnesses = null;

        if (vitrayaInsurerClaimData != null && vitrayaInsurerClaimData.getRequest().getIllness() != null) {
            IllnessDTO illness = vitrayaInsurerClaimData.getRequest().getIllness();
            if (illness.getDefaultICDCode() != null) {
                illnesses = getFirstIllnessByICDCode(illness.getDefaultICDCode());
            } else if (illness.getIllnessName() != null) {
                illnesses = illnessesRepository.findIllnessesByIllnessName(illness.getIllnessName());
            }
        }

        return illnesses != null ?
                new IllnessDTO().updateIllness(illnesses.getId(), illnesses.getDefaultICDCode(), illnesses.getIllnessCode(), illnesses.getIllnessName())
                : null;
    }

    public Illnesses getFirstIllnessByICDCode(String icdCode) {
        List<Illnesses> illnessesList = getIllnessByICDCode(icdCode);
        return illnessesList != null && !illnessesList.isEmpty() ? illnessesList.getFirst() : null;
    }

    public List<Illnesses> getIllnessByICDCode(String icdCode) {
        return illnessesRepository.findIllnessesByDefaultICDCode(icdCode);
    }

    public List<IllnessResultDTO> getIllnessesByProcedureId(String procedureId) {
        return illnessesRepository.findByProcedureId(procedureId);
    }

}
