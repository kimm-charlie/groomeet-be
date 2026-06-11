package com.motd.be.module.admin.admin_file.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.AdminFileException;
import com.motd.be.exception.exceptions.AwsException;
import com.motd.be.exception.exceptions.ImageException;
import com.motd.be.module.admin.admin.entity.Admin;
import com.motd.be.module.admin.admin_file.dto.request.AdminFileUpdateProcessRequestForAdmin;
import com.motd.be.module.admin.admin_file.entity.AdminFileForAdmin;
import com.motd.be.module.admin.banner_file.service.BannerFileCommandServiceForAdmin;
import com.motd.be.module.admin.banner_file.service.BannerFileQueryServiceForAdmin;
import com.motd.be.module.admin.popup_file.service.PopupFileCommandServiceForAdmin;
import com.motd.be.module.admin.popup_file.service.PopupFileQueryServiceForAdmin;
import com.motd.be.module.member.banner_file.entity.BannerFile;
import com.motd.be.module.member.popup_file.entity.PopupFile;
import com.motd.be.shared.aws.dto.GeneratedPresignedUrl;
import com.motd.be.shared.aws.enums.S3DirectoryType;
import com.motd.be.shared.aws.enums.UploadFileType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminFileServiceForAdmin {

	private final BannerFileCommandServiceForAdmin bannerFileCommandServiceForAdmin;
	private final PopupFileCommandServiceForAdmin popupFileCommandServiceForAdmin;
	private final PopupFileQueryServiceForAdmin popupFileQueryServiceForAdmin;
	private final BannerFileQueryServiceForAdmin bannerFileQueryServiceForAdmin;

	public AdminFileForAdmin saveImageByType(S3DirectoryType type, GeneratedPresignedUrl url, Admin admin,
		UploadFileType fileType,
		String fileName, String fileSize) {
		return switch (type) {
			case MEMBER_POPUP_THUMBNAIL, DIRECTOR_POPUP_THUMBNAIL -> popupFileCommandServiceForAdmin.save(
				PopupFile.of(url.getOriginUrl(), url.getFileKey(), admin, fileType, fileName, fileSize,
					type));
			case DIRECTOR_BANNER_THUMBNAIL, DIRECTOR_BANNER_CONTENT, MEMBER_BANNER_THUMBNAIL, MEMBER_BANNER_CONTENT ->
				bannerFileCommandServiceForAdmin.save(
					BannerFile.of(url.getOriginUrl(), url.getFileKey(), admin, fileType, fileName,
						fileSize, type));
			default -> throw new CustomRuntimeException(ImageException.UNSUPPORTED_DIRECTORY_TYPE);
		};
	}

	public void delete(List<? extends AdminFileForAdmin> files, Long adminId) {

		files.forEach(file -> {
			if (!file.isOwnedBy(adminId)) {
				throw new CustomRuntimeException(ImageException.NOT_OWNED_BY);
			}
			file.delete();
		});
	}

	public void updateProcessStatus(String fileKey, AdminFileUpdateProcessRequestForAdmin request) {
		S3DirectoryType directoryType = S3DirectoryType.findByFileKey(fileKey);

		AdminFileForAdmin file = findByFileKey(directoryType, fileKey);

		file.updateProcessStatus(request.getProcessStatus());
	}

	public List<? extends AdminFileForAdmin> findFilesByType(S3DirectoryType directoryType, List<Long> fileIds) {
		return switch (directoryType) {
			case MEMBER_POPUP_THUMBNAIL, DIRECTOR_POPUP_THUMBNAIL ->
				popupFileQueryServiceForAdmin.findAllByIds(fileIds);
			case DIRECTOR_BANNER_THUMBNAIL, DIRECTOR_BANNER_CONTENT, MEMBER_BANNER_THUMBNAIL, MEMBER_BANNER_CONTENT ->
				bannerFileQueryServiceForAdmin.findAllByIdsAdminFileType(fileIds);
			default -> throw new CustomRuntimeException(AdminFileException.UNSUPPORTED_DIRECTORY_TYPE);
		};
	}

	private AdminFileForAdmin findByFileKey(S3DirectoryType type, String fileKey) {
		return switch (type) {
			case MEMBER_POPUP_THUMBNAIL, DIRECTOR_POPUP_THUMBNAIL ->
				popupFileQueryServiceForAdmin.findByFileKey(fileKey);
			case DIRECTOR_BANNER_THUMBNAIL, DIRECTOR_BANNER_CONTENT, MEMBER_BANNER_THUMBNAIL, MEMBER_BANNER_CONTENT ->
				bannerFileQueryServiceForAdmin.findByFileKey(fileKey);
			default -> throw new CustomRuntimeException(AwsException.INVALID_FILE_TYPE);
		};
	}
}
