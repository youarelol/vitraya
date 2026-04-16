package com.vitraya.adjudication.engine.service;

import com.vitraya.adjudication.engine.annotation.SkipResponseLogging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

@Service
@Slf4j
public class HelperService {

    public static BigDecimal safeSubtract(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) return BigDecimal.ZERO;
        if (a == null) return b;
        if (b == null) return a;
        return a.subtract(b);
    }

    public static BigDecimal safeAdd(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) return BigDecimal.ZERO;
        if (a == null) return b;
        if (b == null) return a;
        return a.add(b);
    }

    public static BigDecimal safeAddDefault(BigDecimal baseValue, BigDecimal expectedValue, BigDecimal defaultValue) {
        if (baseValue == null && expectedValue == null && defaultValue == null) return BigDecimal.ZERO;
        if (expectedValue == null && defaultValue == null) return baseValue;
        if (baseValue == null && expectedValue == null) return defaultValue;
        if (baseValue == null && defaultValue == null) return expectedValue;
        return expectedValue == null ? baseValue.add(defaultValue) : baseValue.add(expectedValue);
    }

    public String generateRandomString() {
        String rndm = Integer.toString(new Random().nextInt())
                + (System.currentTimeMillis() / 1000L);
        return hashCal("SHA-256", rndm).substring(10, 20);
    }

    public String hashCal(String type, String str) {
        byte[] hashseq = str.getBytes();
        StringBuffer hexString = new StringBuffer();
        try {
            MessageDigest algorithm = MessageDigest.getInstance(type);
            algorithm.reset();
            algorithm.update(hashseq);
            byte messageDigest[] = algorithm.digest();

            for (int i = 0; i < messageDigest.length; i++) {
                String hex = Integer.toHexString(0xFF & messageDigest[i]);
                if (hex.length() == 1)
                    hexString.append("0");
                hexString.append(hex);
            }
        } catch (NoSuchAlgorithmException nsae) {
            log.error(nsae.getMessage(), nsae);
        }

        return hexString.toString();
    }

    public static void main(String[] args) {
        HelperService helperService = new HelperService();
        System.out.println(helperService.generateRandomString());
    }

    public static boolean areEqualTreatNullAsZero(BigDecimal a, BigDecimal b) {
        return (a == null ? BigDecimal.ZERO : a)
                .compareTo(b == null ? BigDecimal.ZERO : b) == 0;
    }

    @SkipResponseLogging
    public BigDecimal getSafeValue(String value) {
        if (value == null) return BigDecimal.ZERO;
        return new BigDecimal(value);
    }

    public int getSafeIntValue(String value) {
        if (value == null) return 0;
        try {
            return (int) Double.parseDouble(value);
        } catch (NumberFormatException e) {
            log.error("Invalid integer value: {}", value, e);
        }
        return 0;
    }
}
