package com.motd.be.module.member.popup.dto.response;

import com.motd.be.module.member.popup.entity.Popup;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PopupResponse {

	private Long id;
	private String title;
	private String thumbnailImageUrl;
	private String linkUrl;

	public static PopupResponse from(Popup popUp) {
		return PopupResponse.builder()
			.id(popUp.getId())
			.title(popUp.getTitle())
			.thumbnailImageUrl(popUp.getCdnThumbnailImageUrl())
			.linkUrl(popUp.getLinkUrl())
			.build();
	}
}

