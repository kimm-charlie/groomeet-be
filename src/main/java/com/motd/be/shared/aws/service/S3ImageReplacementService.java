package com.motd.be.shared.aws.service;

import java.util.function.Consumer;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.motd.be.common.adpapter.EventPublisherAdapter;
import com.motd.be.common.event_listener.ImageDeleteEventListener;
import com.motd.be.shared.aws.entity.UploadFile;
import com.motd.be.shared.aws.enums.S3DirectoryType;
import com.motd.be.shared.aws.enums.UploadFileType;

import lombok.RequiredArgsConstructor;

/**
 * 해당 서비스는 Presigned URL 방식이 아닌 MultipartFile 방식의 S3 업로드에만 사용됩니다.
 * 이에 해당하는 경우는 profile 또는 관리자가 하는 이미지 업로드입니다.
 * 따라서 해당 서비스를 통해 이미지가 아닌 파일이 삭제되는 경우는 존재하면 안됩니다.
 */
@Component
@RequiredArgsConstructor
public class S3ImageReplacementService {

	private final S3Uploader s3Uploader;
	private final EventPublisherAdapter eventPublisher;

	/**
	 * 이미지 업로드 & DB 업데이트 & 삭제 예약 로직
	 */
	@Transactional
	public void replaceImageAndScheduleDeletion(MultipartFile newImage, String oldUrl, S3DirectoryType dir,
		Consumer<String> updateEntityFn) {
		replaceFileAndScheduleDeletion(newImage, oldUrl, dir, updateEntityFn, UploadFileType.IMAGE);
	}

	private void replaceFileAndScheduleDeletion(MultipartFile newFile, String oldUrl, S3DirectoryType dir,
		Consumer<String> updateEntityFn, UploadFileType fileType) {
		if (newFile == null) {
			return; // 새 파일 없으면 skip
		}

		String newUrl = null;
		try {
			// 1. 새 파일 업로드
			newUrl = s3Uploader.uploadFile(UploadFile.from(newFile, fileType), dir.getDirectoryName());

			// 2. 엔티티 업데이트 (새 URL 반영)
			updateEntityFn.accept(newUrl);

			// 3. 트랜잭션 커밋 이후 기존 파일 삭제 예약
			scheduleImageDeletion(oldUrl, dir);

		} catch (Exception e) {
			// 업로드 도중 예외 → 새 파일 롤백 삭제
			if (newUrl != null) {
				s3Uploader.delete(newUrl, dir);
			}
			throw e;
		}
	}

	@Transactional
	public void scheduleImageDeletion(String oldProfileImageUrl, S3DirectoryType s3DirectoryType) {
		if (oldProfileImageUrl != null) {
			eventPublisher.publish(new ImageDeleteEventListener.ImageDeleteEvent(oldProfileImageUrl, s3DirectoryType));
		}
	}
}
