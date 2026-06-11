package com.motd.be.module.member.report.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.member.entity.Member;

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
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
public class Report {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reporter_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private Member reporter;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reported_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private Member reported;
	@Column(length = 30)
	@Enumerated(EnumType.STRING)
	private ReportType reportType;
	@Column(length = 50)
	@Enumerated(EnumType.STRING)
	private ReportReason reason;
	@Column(length = 1000)
	private String description;
	@CreationTimestamp
	private LocalDateTime createdAt;

	@Builder
	public Report(Member reporter, Member reported, ReportType reportType, ReportReason reason, String description) {
		this.reporter = reporter;
		this.reported = reported;
		this.reason = reason;
		this.reportType = reportType;
		this.description = description;
	}

	public static Report of(Member reporter, Member reported, ReportReason reason, ReportType reportType,
		String description) {
		return Report.builder()
			.reporter(reporter)
			.reported(reported)
			.reason(reason)
			.reportType(reportType)
			.description(description)
			.build();
	}
}
