package com.motd.be.module.member.file.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.motd.be.module.member.file.enums.FileProcessStatus;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.shared.aws.enums.UploadFileType;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@MappedSuperclass
@Getter
public class BaseFile {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	protected Member member;
	protected String originUrl;
	protected String cdnUrl;
	protected String fileKey;
	protected Integer sortOrder;
	@Column(columnDefinition = "boolean default false")
	protected Boolean isDeleted = false;
	@Enumerated(EnumType.STRING)
	@Column(length = 20, nullable = false)
	protected FileProcessStatus processStatus = FileProcessStatus.PENDING;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;
	@Enumerated(EnumType.STRING)
	@Column(length = 20, nullable = false)
	protected UploadFileType fileType;
	protected String fileName;
	protected String fileSize;

	public void updateProcessStatus(FileProcessStatus processStatus) {
		this.processStatus = processStatus;
	}

	public boolean isOwnedBy(Long memberId) {
		return this.member.getId().equals(memberId);
	}

	public void delete() {
		this.isDeleted = true;
	}

}
