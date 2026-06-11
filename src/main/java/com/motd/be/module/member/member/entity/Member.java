package com.motd.be.module.member.member.entity;

import static com.motd.be.common.utils.DateFormatUtils.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.shared.mobile_ok.dto.response.MobileOkResultDto;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
@Table(uniqueConstraints = @UniqueConstraint(name = "unique_member", columnNames = {"activeUniqueKey"}))
@EqualsAndHashCode
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "terms_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private MemberTerms terms;
	@Column(length = 1000)
	private String cdnProfileImageUrl;
	@Column(nullable = false, length = 100)
	private String nickname;
	@Column(length = 100)
	private String email;
	@CreationTimestamp
	private LocalDateTime createdAt;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private Role role;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private SignInPlatform signInPlatform;
	@Column(columnDefinition = "boolean default false")
	private Boolean isWithdrawal;
	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private WithdrawalReason withdrawalReason;
	@Column(columnDefinition = "boolean default false")
	private Boolean isWithdrawalTermsAgreed;
	private LocalDateTime withdrawalAt;
	@Column(length = 100, nullable = false)
	private String identifier;
	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean isBanned;
	private LocalDateTime bannedAt;
	private LocalDate unbannedAt;
	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean isMarketingPushAgreed;
	/**
	 * 같은 platform + identifier 를 가진 회원을 중복해서 저장하지 않기 위해 사용한다.
	 */
	@Column(length = 100)
	private String activeUniqueKey;
	@Column(columnDefinition = "boolean default false")
	private Boolean isDirector;
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "director_info_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private DirectorInfo directorInfo;
	@Column(columnDefinition = "boolean default false")
	private Boolean isAuthenticated;
	private String name;
	private String phoneNumber;
	private LocalDate birth;
	private LocalDateTime authenticatedAt;
	private String authenticationCi;
	private String authenticationDi;
	@Enumerated(EnumType.STRING)
	private Gender genderFromAuthentication;
	@Enumerated(EnumType.STRING)
	private Nation nation;
	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean isActivityPushAgreed;
	@Column(nullable = false, columnDefinition = "bigint default 0")
	private Long cashBalance;
	@Column(nullable = false, columnDefinition = "integer default 0")
	private Integer reportedCount;
	@Column(length = 6, unique = true)
	private String referralCode;

	@Builder
	public Member(MemberTerms terms, String nickname, String email, Role role, SignInPlatform signInPlatform,
		Boolean isWithdrawal, WithdrawalReason withdrawalReason, Boolean isWithdrawalTermsAgreed,
		LocalDateTime withdrawalAt, String identifier, Boolean isBanned, LocalDateTime bannedAt,
		Boolean isMarketingPushAgreed, String activeUniqueKey, Boolean isDirector, DirectorInfo directorInfo,
		String cdnProfileImageUrl, Boolean isAuthenticated, String name, String phoneNumber, LocalDate birth,
		LocalDateTime authenticatedAt, String authenticationCi, String authenticationDi, Nation nation,
		Boolean isActivityPushAgreed, Long cashBalance, LocalDate unbannedAt, Integer reportedCount,
		String referralCode, Gender genderFromAuthentication) {
		this.terms = terms;
		this.cdnProfileImageUrl = cdnProfileImageUrl;
		this.nickname = nickname;
		this.email = email;
		this.role = role;
		this.signInPlatform = signInPlatform;
		this.isWithdrawal = isWithdrawal;
		this.withdrawalReason = withdrawalReason;
		this.isWithdrawalTermsAgreed = isWithdrawalTermsAgreed;
		this.withdrawalAt = withdrawalAt;
		this.identifier = identifier;
		this.isBanned = isBanned;
		this.bannedAt = bannedAt;
		this.isMarketingPushAgreed = isMarketingPushAgreed;
		this.activeUniqueKey = activeUniqueKey;
		this.isDirector = isDirector;
		this.directorInfo = directorInfo;
		this.isAuthenticated = isAuthenticated;
		this.name = name;
		this.phoneNumber = phoneNumber;
		this.birth = birth;
		this.authenticatedAt = authenticatedAt;
		this.authenticationCi = authenticationCi;
		this.authenticationDi = authenticationDi;
		this.nation = nation;
		this.isActivityPushAgreed = isActivityPushAgreed;
		this.cashBalance = cashBalance;
		this.unbannedAt = unbannedAt;
		this.reportedCount = reportedCount;
		this.referralCode = referralCode;
		this.genderFromAuthentication = genderFromAuthentication;
	}

	public void updateNickname(String nickname) {
		this.nickname = nickname;
	}

	public void updateWithdrawalInfo(WithdrawalReason withdrawalReason) {
		this.withdrawalAt = LocalDateTime.now();
		this.withdrawalReason = withdrawalReason;
		this.isWithdrawal = true;
		this.activeUniqueKey = null;
	}

	public boolean isWithdrawn() {
		return Boolean.TRUE.equals(this.isWithdrawal);
	}

	public boolean isSignInPlatformApple() {
		return this.signInPlatform.equals(SignInPlatform.APPLE);
	}

	public void updateProfileImage(String cdnUrl) {
		this.cdnProfileImageUrl = cdnUrl;
	}

	public boolean isDirector() {
		return this.isDirector && this.directorInfo != null;
	}

	public void registerAsDirector(DirectorInfo directorInfo) {
		this.directorInfo = directorInfo;
		this.isDirector = true;
		this.role = Role.DIRECTOR;
	}

	public void updatePushSetting(PushType pushType) {
		switch (pushType) {
			case MARKETING_PUSH -> this.isMarketingPushAgreed = !this.isMarketingPushAgreed;
			case ACTIVITY_PUSH -> this.isActivityPushAgreed = !this.isActivityPushAgreed;
		}

	}

	public void updateCash(long newBalance) {
		this.cashBalance = newBalance;
	}

	public void setAuthenticated(MobileOkResultDto mobileOkResultDto) {
		this.name = mobileOkResultDto.getUserName();
		this.phoneNumber = mobileOkResultDto.getUserPhone();
		this.birth = parseToLocalDateFromMobileOk(mobileOkResultDto.getUserBirthday());
		this.genderFromAuthentication = Gender.findByCode(mobileOkResultDto.getUserGender());
		this.authenticatedAt = parseToLocalDateTimeFromAuthenticatedAt(mobileOkResultDto.getIssueDate());
		this.authenticationCi = mobileOkResultDto.getCi();
		this.authenticationDi = mobileOkResultDto.getDi();
		this.nation = Nation.findByCode(mobileOkResultDto.getUserNation());
		this.isAuthenticated = true;
	}

	public void toggleAuthentication() {
		if (this.isAuthenticated) {
			this.authenticationCi = null;
			this.isAuthenticated = false;
		}
	}

	public void ban(BanPeriod banPeriod) {
		this.isBanned = true;
		this.bannedAt = LocalDateTime.now();
		this.unbannedAt = banPeriod.calculateUnbannedAt();
	}
}
