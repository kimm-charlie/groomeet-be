package com.motd.be.shared.aws.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.AwsException;
import com.motd.be.shared.aws.entity.UploadFile;
import com.motd.be.shared.aws.enums.S3DirectoryType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3Uploader {

	private final S3Client s3Client;
	@Value("${aws.s3.image-origin-bucket}")
	private String imageOriginBucket;
	@Value("${aws.s3.public-bucket}")
	private String publicBucket;

	public String uploadFile(UploadFile uploadFile, String directory) {
		final String fileName = directory + UUID.randomUUID() + "." + uploadFile.getContentType().split("/")[1];
		putS3(uploadFile, fileName);
		return getObjectUrl(fileName);
	}

	private void putS3(UploadFile uploadFile, final String fileName) {
		RequestBody requestBody = RequestBody.fromBytes(uploadFile.getFileBytes());
		try {
			PutObjectRequest putObjectRequest = createPutObjRequest(uploadFile, fileName);
			s3Client.putObject(putObjectRequest, requestBody);
		} catch (Exception e) {
			throw new CustomRuntimeException(AwsException.S3_FILE_UPLOAD_FAIL);
		}
	}

	private PutObjectRequest createPutObjRequest(UploadFile uploadFile, String fileName) {
		return PutObjectRequest.builder()
			.bucket(imageOriginBucket)
			.contentType(uploadFile.getContentType())
			.contentLength(uploadFile.getFileSize())
			.key(fileName)
			.build();
	}

	private String getObjectUrl(final String fileName) {
		GetUrlRequest getUrlRequest = GetUrlRequest.builder()
			.bucket(imageOriginBucket)
			.key(fileName)
			.build();
		return s3Client.utilities().getUrl(getUrlRequest).toString();
	}

	/**
	 * 무조건 image 파일만 온다고 가정한다.
	 *
	 * @param imageUrl
	 * @param s3DirectoryType
	 */
	public void delete(String imageUrl, S3DirectoryType s3DirectoryType) {
		try {
			String encodedName = imageUrl.split("/")[s3DirectoryType.getUrlIndex()];
			String decodedName = URLDecoder.decode(encodedName, StandardCharsets.UTF_8);
			String fileName = s3DirectoryType.getDirectoryName() + decodedName;

			DeleteObjectRequest deleteOriginBucketRequest = DeleteObjectRequest.builder()
				.bucket(imageOriginBucket)
				.key(fileName)
				.build();

			DeleteObjectRequest deletePublicBucketRequest = DeleteObjectRequest.builder()
				.bucket(publicBucket)
				.key(fileName.replaceAll("\\.[^.]+$", ".webp"))
				.build();

			s3Client.deleteObject(deleteOriginBucketRequest);
			s3Client.deleteObject(deletePublicBucketRequest);
		} catch (Exception e) {
			log.error("Failed to delete S3 object for URL: {}, DirectoryType : {}", imageUrl,
				s3DirectoryType.getDirectoryName());
		}
	}

}
