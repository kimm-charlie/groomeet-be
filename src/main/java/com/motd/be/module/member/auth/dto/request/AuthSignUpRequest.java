package com.motd.be.module.member.auth.dto.request;

import static com.motd.be.common.constants.DefaultConstants.*;
import static com.motd.be.common.constants.ValidationMessages.*;
import static com.motd.be.common.utils.Utils.*;
import static com.motd.be.shared.aws.util.ImageUrlConverter.*;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.MemberTerms;
import com.motd.be.module.member.member.entity.Role;
import com.motd.be.module.member.member.entity.SignInPlatform;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class AuthSignUpRequest {

	@NotBlank(message = UUID_REQUIRED)
	private String uuid;
	@NotNull(message = SERVICE_AGREEMENT_REQUIRED)
	private Boolean serviceAgreed;
	@NotNull(message = PRIVACY_POLICY_AGREEMENT_REQUIRED)
	private Boolean privacyPolicyAgreed;
	@NotNull(message = MARKETING_AGREEMENT_REQUIRED)
	private Boolean marketingAgreed;
	private String referralCode;

	public Member toMemberEntity(String identifier, String email, String nickname, Role role,
		SignInPlatform signInPlatform, MemberTerms memberTerms, String referralCode) {
		return Member.builder()
			.terms(memberTerms)
			.nickname(nickname)
			.email(email)
			.role(role)
			.signInPlatform(signInPlatform)
			.identifier(identifier)
			.isMarketingPushAgreed(this.marketingAgreed)
			.isActivityPushAgreed(Boolean.TRUE)
			.activeUniqueKey(generateMemberActiveUniqueKey(signInPlatform, identifier))
			.cdnProfileImageUrl(toCdnUrl(DEFAULT_PROFILE_IMAGE_URL))
			.referralCode(referralCode)
			.build();
	}

	public MemberTerms toMemberTermsEntity() {
		return MemberTerms.builder()
			.serviceAgreed(this.serviceAgreed)
			.privacyPolicyAgreed(this.privacyPolicyAgreed)
			.build();
	}

}
