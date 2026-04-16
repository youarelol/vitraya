package com.vitraya.adjudication.engine.dto.request;

import lombok.Data;

@Data
public class PatientDetails {
    private String patientName;
    private String policyType;
    private String gender;
    private String dob;
    private Object companyName;
    private String policyNumber;
    private Object employeeId;
    private String medicalIdCard;

    public String getPatientName() {
        return patientName;
    }


    public String getGender() {
        return gender;
    }

    public String getDob() {
        return dob;
    }

    public String getPolicyType() {
        return policyType;
    }

    public Object getCompanyName() {
        return companyName;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public Object getEmployeeId() {
        return employeeId;
    }

    public String getMedicalIdCard() {
        return medicalIdCard;
    }
}
