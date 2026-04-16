package com.vitraya.adjudication.engine.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ClaimIllnessTreatmentDetailsDTO {
	private int claimId;
	private ChronicIllnessDetailsJSON chronicIllnessDetailsJSON;
	private String lineOfTreatmentDetails;
	private String dateOfDiagnosis;
	private String doctorsDetails;
	private String hospitalizationType;
	private boolean maternityCase;
	private String accidentDate;


	@Data
	public static class DoctorDto {
		private String doctorName;
		private String qualification;
		private String dateOfSurgery;
		private String amountToClaim;
	}

	public enum ManagementType {
		SURGICAL("Surgical Management"),
		MEDICAL("Medical Management");

		String managementType;

		ManagementType(String managementType) {
			this.managementType = managementType;
		}

		public String getManagementType() {
			return managementType;
		}
	}
}