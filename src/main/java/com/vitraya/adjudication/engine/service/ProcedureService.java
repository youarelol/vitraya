package com.vitraya.adjudication.engine.service;

import com.vitraya.adjudication.engine.dto.enums.VitrayaErrorCodes;
import com.vitraya.adjudication.engine.dto.request.ProcedureDTO;
import com.vitraya.adjudication.engine.dto.request.VitrayaInsurerClaimDataDTO;
import com.vitraya.adjudication.engine.helper.VitrayaException;
import com.vitraya.adjudication.engine.mysql.entity.Procedures;
import com.vitraya.adjudication.engine.mysql.impl.ProcedureDataRepositoryImpl;
import com.vitraya.adjudication.engine.mysql.repository.ProceduresRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class ProcedureService {
    public final ProceduresRepository proceduresRepository;
    private final ProcedureDataRepositoryImpl procedureDataRepositoryImpl;

    public ProcedureService(ProceduresRepository proceduresRepository, ProcedureDataRepositoryImpl procedureDataRepositoryImpl) {
        this.proceduresRepository = proceduresRepository;
        this.procedureDataRepositoryImpl = procedureDataRepositoryImpl;
    }

    public ProcedureDTO getProcedure(VitrayaInsurerClaimDataDTO vitrayaInsurerClaimData) {
        Procedures procedures = null;
        if (vitrayaInsurerClaimData.getRequest().getProcedure() != null) {
            if (vitrayaInsurerClaimData.getRequest().getProcedure().getSnowmedCode() != null) {
                procedures = findProceduresByVneuronSctidCode(
                        vitrayaInsurerClaimData.getRequest().getProcedure().getSnowmedCode());
            } else if (vitrayaInsurerClaimData.getRequest().getProcedure().getName() != null) {
                procedures = findProceduresByName(
                        vitrayaInsurerClaimData.getRequest().getProcedure().getName());
            }
        }

        vitrayaInsurerClaimData.getRequest().getClaim().setInScopeProcedure(!(procedures == null));
        return procedures == null ? null : vitrayaInsurerClaimData.getRequest().getProcedure().updateProcedure(
                procedures.getId(), procedures.getVneuronSctidCode(), procedures.getName(), procedures.getProcedureCode());
    }

    public Procedures findProceduresByVneuronSctidCode(String snowmedCode) {
        return proceduresRepository.findProceduresByVneuronSctidCode(snowmedCode);
    }

    public Procedures findProceduresByName(String name) {

        return null;
    }

    public Optional<Procedures> findProcedureById(long procedureId) {
        return proceduresRepository.findById(procedureId);
    }

    public List<Procedures> findAllProcedures() {
        return proceduresRepository.findAllNotDeleted();
    }

    public Map<String, Object> getAllProcedures() {
        return procedureDataRepositoryImpl.getAllProcedures().orElseThrow(() -> new VitrayaException(VitrayaErrorCodes.NO_PROCEDURE_FOUND));
    }

    public Map<String, Object> getProceduresIllnesses(long prcId) {
        return procedureDataRepositoryImpl.getProcedureIllnesses(prcId).orElseThrow(() -> new VitrayaException(VitrayaErrorCodes.PROCEDURE_HAS_NO_ILLNESS));
    }
}
