package com.vitraya.adjudication.engine.service;

import com.vitraya.adjudication.engine.dto.request.InsurerEncryptedClaimRequest;
import com.vitraya.adjudication.engine.utils.EncryptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IframeService {
    private final EncryptionUtils encryptionUtils;

    public IframeService(EncryptionUtils encryptionUtils) {
        this.encryptionUtils = encryptionUtils;
    }

    public InsurerEncryptedClaimRequest getDecryptedIframeData(String encryptedData) throws Exception {
        log.info("encryptedData String - {}", encryptedData);
        return encryptionUtils.decryptIframeData(encryptedData);
    }
}
