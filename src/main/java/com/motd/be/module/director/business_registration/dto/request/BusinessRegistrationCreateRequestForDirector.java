package com.motd.be.module.director.business_registration.dto.request;

import static com.motd.be.common.constants.ValidationConstants.*;
import static com.motd.be.common.constants.ValidationMessages.*;

import java.util.List;

import org.hibernate.validator.constraints.Length;

import com.motd.be.module.member.business_registration.entity.BusinessRegistration;
import com.motd.be.module.member.member.entity.Member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessRegistrationCreateRequestForDirector {

	@NotBlank(message = BUSINESS_REGISTRATION_NUMBER_REQUIRED)
	@Length(max = BUSINESS_REGISTRATION_NUMBER_LENGTH,
		message = BUSINESS_REGISTRATION_NUMBER_MAX_LENGTH_MSG)
	private String businessRegistrationNumber;
	@NotBlank(message = RESIDENT_REGISTRATION_NUMBER_REQUIRED)
	@Length(max = BUSINESS_REGISTRATION_RESIDENT_NUMBER_LENGTH,
		message = RESIDENT_REGISTRATION_NUMBER_MAX_LENGTH_MSG)
	private String residentRegistrationNumber;
	@NotEmpty(message = BUSINESS_REGISTRATION_FILE_REQUIRED)
	@Size(max = BUSINESS_REGISTRATION_FILE_MAX_COUNT, message = BUSINESS_REGISTRATION_FILE_MAX_COUNT_MSG)
	private List<Long> fileIds;

	public BusinessRegistration toEntity(Member member, String encryptedResidentRegistrationNumber) {
		return BusinessRegistration.builder()
			.member(member)
			.businessRegistrationNumber(businessRegistrationNumber)
			.residentRegistrationNumber(encryptedResidentRegistrationNumber)
			.build();
	}
}
