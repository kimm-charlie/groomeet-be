package com.motd.be.module.member.file.dto.response;

import com.motd.be.module.member.file.enums.FileProcessStatus;
import com.motd.be.shared.aws.enums.S3DirectoryType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class FileProcessStatusResponse {

	private Long fileId;
	private String directoryType;
	private FileProcessStatus processStatus;
	private boolean processed;

	public static FileProcessStatusResponse of(Long fileId, S3DirectoryType directoryType,
		FileProcessStatus processStatus) {
		return FileProcessStatusResponse.builder()
			.fileId(fileId)
			.directoryType(directoryType.name())
			.processStatus(processStatus)
			.processed(FileProcessStatus.PROCESSED.equals(processStatus))
			.build();
	}
}

