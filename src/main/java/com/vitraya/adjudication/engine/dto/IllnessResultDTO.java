package com.vitraya.adjudication.engine.dto;

public class IllnessResultDTO {
    private String defaultIcdCode;
    private String category;
    private String name;
    private String illnessCode;

    public IllnessResultDTO(String defaultIcdCode, String category, String name, String illnessCode) {
        this.defaultIcdCode = defaultIcdCode;
        this.category = category;
        this.name = name;
        this.illnessCode = illnessCode;
    }

    // Getters
    public String getDefaultIcdCode() {
        return defaultIcdCode;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getIllnessCode() {
        return illnessCode;
    }
}
