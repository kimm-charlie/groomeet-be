package com.motd.be.module.admin.popup.dto.response;

import java.util.List;
import java.util.stream.Collectors;

import com.motd.be.module.member.popup.entity.Popup;
import com.motd.be.module.member.popup.entity.PopupType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PopupAdminResponseForAdmin {

	private Long id;
	private String title;
	private String thumbnailImageUrl;
	private String linkUrl;
	private PopupType type;
	private Long thumbnailFileId;
	private String startAt;
	private String createdAt;
	private String endAt;
	private Integer sortOrder;
	private Boolean isDeleted;

	public static List<PopupAdminResponseForAdmin> fromList(List<Popup> popups) {
		return popups.stream()
			.map(PopupAdminResponseForAdmin::from)
			.collect(Collectors.toList());
	}

	public static PopupAdminResponseForAdmin from(Popup popup) {
		return PopupAdminResponseForAdmin.builder()
			.id(popup.getId())
			.title(popup.getTitle())
			.thumbnailImageUrl(popup.getCdnThumbnailImageUrl())
			.linkUrl(popup.getLinkUrl())
			.type(popup.getType())
			.thumbnailFileId(popup.getThumbnailFile() != null ? popup.getThumbnailFile().getId() : null)
			.createdAt(popup.getFormattedCreatedAt())
			.startAt(popup.getFormattedStartAt())
			.endAt(popup.getFormattedEndAt())
			.isDeleted(popup.getIsDeleted())
			.sortOrder(popup.getSortOrder())
			.build();
	}
}


