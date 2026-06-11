package com.motd.be.module.member.director_info.dto.request;

import static com.motd.be.common.constants.ValidationMessages.*;
import static com.motd.be.common.utils.Utils.*;

import java.util.List;

import org.hibernate.validator.constraints.Length;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_profile_detail.entity.DirectorProfileDetail;
import com.motd.be.module.member.member.entity.Gender;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class DirectorInfoRegisterRequest {

	@NotBlank(message = NICKNAME_REQUIRED)
	@Length(max = 12, message = NICKNAME_MAX_LENGTH)
	private String nickname;
	@NotEmpty(message = LOCATION_MUST_BE_SELECTED)
	private List<Long> locationIds;
	@NotEmpty(message = DIRECTOR_SERVICE_MUST_BE_SELECTED)
	@Size(max = 7, message = DIRECTOR_SERVICE_SELECTION_OUT_OF_BOUNDS)
	private List<Long> directorServiceIds;
	@NotBlank(message = GENDER_MUST_BE_SELECTED)
	private String gender;
	private String phoneNumber;

	public DirectorInfo toEntity() {
		return DirectorInfo.builder()
			.gender(Gender.valueOf(this.gender))
			.directorProfileDetail(DirectorProfileDetail.builder().build())
			.tempPhoneNumber(formatPhoneNumber(phoneNumber))
			.onboardingPassEndsAt(resolveOnboardingPassEndsAt())
			.build();
	}
}
