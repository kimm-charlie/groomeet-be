package com.motd.be.shared.aws.service;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.constants.TimePolicy.*;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.motd.be.shared.aws.dto.GeneratedPresignedUrl;
import com.motd.be.shared.aws.enums.S3DirectoryType;
import com.motd.be.shared.aws.enums.UploadFileType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3PresignedUrlService {

	private final S3Presigner s3Presigner;
	@Value("${aws.s3.image-origin-bucket}")
	private String imageOriginBucketName;
	@Value("${aws.s3.public-bucket}")
	private String publicBucketName;
	@Value("${spring.cloud.aws.region.static}")
	private String region;

	public GeneratedPresignedUrl generatePresignedUrl(S3DirectoryType s3DirectoryType, UploadFileType fileType,
		String fileExtension, long fileSizeBytes) {
		String uniqueFileName = UUID.randomUUID() + "." + fileExtension;
		String objectKey = buildObjectKey(s3DirectoryType.getDirectoryName(), uniqueFileName);
		String bucketName = determineBucketName(fileType, fileSizeBytes);
		boolean isPublicBucket = bucketName.equals(publicBucketName);

		try {
			PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName).key(objectKey).build();

			PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
				.signatureDuration(PRESIGNED_URL_EXPIRATION)
				.putObjectRequest(putObjectRequest)
				.build();

			PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
			String presignedUrl = presignedRequest.url().toString();
			String originUrl = buildFileUrl(objectKey, bucketName);

			return GeneratedPresignedUrl.of(presignedUrl, originUrl, objectKey, isPublicBucket);
		} catch (Exception e) {
			log.error("Failed to generate presigned URL for file: {} in directory: {} with type: {}", uniqueFileName,
				s3DirectoryType.name(), fileType.name(), e);
			throw new RuntimeException("Presigned URL 생성에 실패했습니다.", e);
		}
	}

	/**
	 * S3 Object Key 생성
	 */
	private String buildObjectKey(String directory, String fileName) {
		if (directory == null || directory.trim().isEmpty()) {
			return fileName;
		}
		return String.format("%s%s", directory.trim(), fileName);
	}

	/**
	 * 파일 접근 URL 생성
	 */
	private String buildFileUrl(String objectKey, String bucketName) {
		return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, objectKey);
	}

	/**
	 * 버킷 결정 로직
	 * - 이미지이면서 1MB 이상: Lambda 리사이징 버킷 (imageOriginBucketName)
	 * - 이미지이면서 1MB 미만: public 버킷 (리사이징 스킵)
	 * - 이미지 외 파일: public 버킷
	 */
	private String determineBucketName(UploadFileType fileType, long fileSizeBytes) {
		if (fileType == UploadFileType.IMAGE && fileSizeBytes >= IMAGE_RESIZE_THRESHOLD_BYTES) {
			return imageOriginBucketName;
		}
		return publicBucketName;
	}

	/**
	 * 파일 크기 제한 검증 (MB 단위)
	 */
	public boolean isValidFileSize(long fileSizeBytes, int maxSizeMB) {
		long maxSizeBytes = maxSizeMB * 1024 * 1024L;
		return fileSizeBytes <= maxSizeBytes;
	}
}
