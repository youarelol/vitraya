package com.vitraya.adjudication.engine.service;

import com.vitraya.adjudication.engine.dto.request.VitrayaInsurerClaimDataDTO;
import com.vitraya.adjudication.engine.mysql.entity.ClaimData;
import com.vitraya.adjudication.engine.mysql.entity.ClaimTransition;
import com.vitraya.adjudication.engine.mysql.repository.ClaimTransitionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class ClaimTransitionService {
    private final ClaimTransitionRepository claimTransitionRepository;

    public ClaimTransitionService(ClaimTransitionRepository claimTransitionRepository) {
        this.claimTransitionRepository = claimTransitionRepository;
    }


    public void saveClaimTransition(ClaimData claimData, VitrayaInsurerClaimDataDTO vitrayaInsurerClaimData) {
        log.info("Saving claim transition data");
        saveClaimTransitionData(claimData.getId(), vitrayaInsurerClaimData.getRequest().getClaim().getClaimStatusText(),
                vitrayaInsurerClaimData.getTxnId());
    }

    public void saveClaimTransitionData(long claimDataId, String status, String txnId) {
        log.info("Saving claim transition data");
        ClaimTransition claimTransition = new ClaimTransition();
        claimTransition.setClaimDataId(claimDataId);
        claimTransition.setStatus(status);
        claimTransition.setDateCreated(new Date());
        claimTransition.setTxnId(txnId);
        claimTransitionRepository.save(claimTransition);
    }


    public List<ClaimTransition> findAllClaimTransitionByClaimDataIdLatest(long claimId) {
        return claimTransitionRepository.findAllClaimTransitionByClaimDataIdLatest(claimId);
    }
}
