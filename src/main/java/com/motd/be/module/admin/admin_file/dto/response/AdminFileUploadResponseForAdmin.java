package com.motd.be.module.admin.admin_file.dto.response;

import com.motd.be.module.admin.admin_file.entity.AdminFileForAdmin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminFileUploadResponseForAdmin {

	private String presignedUrl;
	private Long imageId;
	private String cdnUrl;

	public static AdminFileUploadResponseForAdmin of(String presignedUrl, AdminFileForAdmin image) {
		return AdminFileUploadResponseForAdmin.builder()
			.presignedUrl(presignedUrl)
			.imageId(image.getId())
			.cdnUrl(image.getCdnUrl())
			.build();
	}
}
