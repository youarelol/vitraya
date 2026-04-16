package com.vitraya.adjudication.engine.service;

import com.vitraya.adjudication.engine.dto.enums.VitrayaErrorCodes;
import com.vitraya.adjudication.engine.helper.VitrayaException;
import com.vitraya.adjudication.engine.mysql.entity.PolicyProduct;
import com.vitraya.adjudication.engine.mysql.impl.PolicyProductDataRepositoryImpl;
import com.vitraya.adjudication.engine.mysql.repository.PolicyProductRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PolicyProductService {

    private final PolicyProductRepository policyProductRepository;
    private final PolicyProductDataRepositoryImpl policyProductDataRepositoryImpl;

    public PolicyProductService(PolicyProductRepository policyProductRepository, PolicyProductDataRepositoryImpl policyProductDataRepositoryImpl) {
        this.policyProductRepository = policyProductRepository;
        this.policyProductDataRepositoryImpl = policyProductDataRepositoryImpl;
    }

    public PolicyProduct getPolicyProduct(String policyName) {
        return policyProductRepository.findByPolicyName(policyName);
    }

    public Map<String, Object> getAllPolicies() {
        return policyProductDataRepositoryImpl.getAllPolicies().orElseThrow(() -> new VitrayaException(VitrayaErrorCodes.NO_POLICY_FOUND));
    }

}
