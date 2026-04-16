package com.vitraya.adjudication.engine.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ChangeCodeGenerator {

    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public static String generateUniqueString(Object item) {
        String base = item.toString() + System.nanoTime() + UUID.randomUUID();
        long hash = Math.abs(base.hashCode() ^ base.hashCode() >>> 16); // mix bits
        return toBase62(hash).substring(0, 12); // ensure length 12
    }

    private static String toBase62(long value) {
        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            sb.append(BASE62.charAt((int) (value % 62)));
            value /= 62;
        }
        while (sb.length() < 12) {
            sb.append('0'); // pad with '0' if too short
        }
        return sb.reverse().toString();
    }

}
