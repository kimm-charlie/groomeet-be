package com.motd.be.module.member.business_registration.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.business_registration_file.entity.BusinessRegistrationFile;
import com.motd.be.module.member.member.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
public class BusinessRegistration {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false, unique = true,
		foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private Member member;
	@Column(nullable = false, length = 50)
	private String businessRegistrationNumber;
	@Column(nullable = false, length = 512)
	private String residentRegistrationNumber;
	@CreationTimestamp
	private LocalDateTime createdAt;
	@OneToMany(mappedBy = "businessRegistration", fetch = FetchType.LAZY)
	private List<BusinessRegistrationFile> files;

	@Builder
	public BusinessRegistration(Member member, String businessRegistrationNumber, String residentRegistrationNumber) {
		this.member = member;
		this.businessRegistrationNumber = businessRegistrationNumber;
		this.residentRegistrationNumber = residentRegistrationNumber;
	}
}
