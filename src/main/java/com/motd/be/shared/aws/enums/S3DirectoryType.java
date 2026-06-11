package com.motd.be.shared.aws.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.AwsException;
import com.motd.be.exception.exceptions.FileException;

import lombok.Getter;

@Getter
public enum S3DirectoryType {

	PROFILE("profile/", "프로필 이미지", 4, UploadFileType.IMAGE),
	PORTFOLIO("portfolio/", "포트폴리오 이미지", 4, UploadFileType.IMAGE),
	CHAT("chat/", "채팅 이미지", 4, UploadFileType.IMAGE, UploadFileType.DOCUMENT),
	SERVICE_ESTIMATE("service-estimate/", "제안서 파일", 4, UploadFileType.IMAGE),
	REVIEW("review/", "리뷰 이미지", 4, UploadFileType.IMAGE),
	SERVICE_REQUEST("service-request/", "서비스 요청 파일", 4, UploadFileType.IMAGE),
	REPORT("report/", "신고 파일", 4, UploadFileType.IMAGE),
	DIRECTOR_PROFILE_DETAIL("director-profile-detail/", "디렉터 프로필 상세 파일", 4, UploadFileType.IMAGE),
	BUSINESS_REGISTRATION("business-registration/", "사업자 등록증 파일", 4, UploadFileType.IMAGE, UploadFileType.DOCUMENT),
	MEMBER_POPUP_THUMBNAIL("popup/member/thumbnail/", "팝업 썸네일 이미지", 6, UploadFileType.IMAGE),
	DIRECTOR_POPUP_THUMBNAIL("popup/director/thumbnail/", "팝업 썸네일 이미지", 6, UploadFileType.IMAGE),
	DIRECTOR_BANNER_THUMBNAIL("banner/director/thumbnail/", "디렉터 배너 썸네일 이미지", 6, UploadFileType.IMAGE),
	DIRECTOR_BANNER_CONTENT("banner/director/content/", "디렉터 배너 컨텐트 이미지", 6, UploadFileType.IMAGE),
	MEMBER_BANNER_THUMBNAIL("banner/member/thumbnail/", "멤버 배너 썸네일 이미지", 6, UploadFileType.IMAGE),
	MEMBER_BANNER_CONTENT("banner/member/content/", "멤버 배너 컨텐트 이미지", 6, UploadFileType.IMAGE),
	FILE("file/", "관리자 파일", 4, UploadFileType.IMAGE),
	CONSULTING_REQUEST("events/consulting-request/", "컨설팅 요청 파일", 5, UploadFileType.IMAGE),
	CONSULTING_SHEET("events/consulting-sheet/", "컨설팅지 파일", 5, UploadFileType.IMAGE);

	private final String directoryName;
	private final String description;
	private final int urlIndex; // split("/") 결과 중 실제 파일명이 들어가는 인덱스
	private final Set<UploadFileType> allowedFileTypes;

	S3DirectoryType(String directoryName, String description, int urlIndex, UploadFileType... allowedFileTypes) {
		this.directoryName = directoryName;
		this.description = description;
		this.urlIndex = urlIndex;
		this.allowedFileTypes = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(allowedFileTypes)));
	}

	public static S3DirectoryType findByName(String directoryName) {
		for (S3DirectoryType directoryType : values()) {
			if (directoryType.name().equalsIgnoreCase(directoryName)) {
				return directoryType;
			}
		}
		throw new CustomRuntimeException(AwsException.S3_DIRECTORY_TYPE_NOT_FOUND);
	}

	public static S3DirectoryType findByFileKey(String fileKey) {
		for (S3DirectoryType directoryType : values()) {
			if (fileKey.startsWith(directoryType.directoryName)) {
				return directoryType;
			}
		}
		throw new CustomRuntimeException(FileException.INVALID_FILE_KEY);
	}

	public void validateFileType(UploadFileType fileType) {
		if (!allowedFileTypes.contains(fileType)) {
			throw new CustomRuntimeException(AwsException.INVALID_FILE_TYPE);
		}
	}
}
