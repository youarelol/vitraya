package com.vitraya.adjudication.engine.dto.enums;

public enum Zone {
    A, B, C, I, II, III;

    public static Zone getCopayZone(String zoneStr) {
        for (Zone zone : values()) {
            if (zone.name().equals(zoneStr)) {
                return zone;
            }
        }
        return null;
    }

    public static Zone getCopayZoneValue(String policyZone) {
        if (policyZone != null) {
            String[] split = policyZone.split(" ");
            policyZone = split.length > 1 ? split[1] : policyZone;
            for (Zone zone : values()) {
                if (zone.name().equals(policyZone)) {
                    return zone;
                }
            }
        }

        return null;
    }
}
