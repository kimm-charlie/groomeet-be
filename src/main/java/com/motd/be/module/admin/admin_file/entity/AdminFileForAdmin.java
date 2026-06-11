package com.motd.be.module.admin.admin_file.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.motd.be.module.admin.admin.entity.Admin;
import com.motd.be.module.member.file.enums.FileProcessStatus;
import com.motd.be.shared.aws.enums.S3DirectoryType;
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
public class AdminFileForAdmin {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	protected Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "admin_id", nullable = false)
	protected Admin admin;
	@Column(nullable = false, length = 1000)
	protected String originUrl;
	@Column(nullable = false, length = 1000)
	protected String cdnUrl;
	@Column(nullable = false, length = 1000)
	protected String fileKey;
	@Column(nullable = false, columnDefinition = "boolean default false")
	protected Boolean isDeleted;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	protected FileProcessStatus processStatus = FileProcessStatus.PENDING;
	@CreationTimestamp
	@Column(updatable = false)
	protected LocalDateTime createdAt;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	protected UploadFileType fileType;
	@Column(length = 100)
	protected String fileName;
	@Column(length = 20)
	protected String fileSize;
	@Enumerated(EnumType.STRING)
	@Column(length = 50)
	protected S3DirectoryType directoryType;

	public void updateProcessStatus(FileProcessStatus processStatus) {
		this.processStatus = processStatus;
	}

	public boolean isOwnedBy(Long adminId) {
		return this.admin.getId().equals(adminId);
	}

	public void delete() {
		this.isDeleted = true;
	}
}
