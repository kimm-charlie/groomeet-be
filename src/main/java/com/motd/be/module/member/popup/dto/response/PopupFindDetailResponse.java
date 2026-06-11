package com.motd.be.module.member.popup.dto.response;

import com.motd.be.module.member.popup.entity.Popup;
import com.motd.be.module.member.popup.entity.PopupType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PopupFindDetailResponse {

	private Long id;
	private String title;
	private String thumbnailImageUrl;
	private String linkUrl;
	private PopupType type;

	public static PopupFindDetailResponse from(Popup popUp) {
		return PopupFindDetailResponse.builder()
			.id(popUp.getId())
			.title(popUp.getTitle())
			.thumbnailImageUrl(popUp.getCdnThumbnailImageUrl())
			.linkUrl(popUp.getLinkUrl())
			.type(popUp.getType())
			.build();
	}
}


