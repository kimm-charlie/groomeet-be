package com.motd.be.module.director.popup.dto.response;

import java.util.List;

import com.motd.be.module.member.popup.entity.Popup;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PopupResponseForDirector {

	private Long id;
	private String title;
	private String thumbnailImageUrl;
	private String linkUrl;

	public static PopupResponseForDirector from(Popup popup) {
		return PopupResponseForDirector.builder()
			.id(popup.getId())
			.title(popup.getTitle())
			.thumbnailImageUrl(popup.getCdnThumbnailImageUrl())
			.linkUrl(popup.getLinkUrl())
			.build();
	}

	public static List<PopupResponseForDirector> fromList(List<Popup> popups) {
		return popups.stream()
			.map(PopupResponseForDirector::from)
			.toList();
	}
}
