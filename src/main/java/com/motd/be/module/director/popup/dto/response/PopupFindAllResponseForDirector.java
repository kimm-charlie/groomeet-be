package com.motd.be.module.director.popup.dto.response;

import java.util.List;

import com.motd.be.module.member.popup.entity.Popup;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PopupFindAllResponseForDirector {

	private List<PopupResponseForDirector> popups;
	private int totalCount;

	public static PopupFindAllResponseForDirector from(List<Popup> popups) {
		return PopupFindAllResponseForDirector.builder()
			.popups(PopupResponseForDirector.fromList(popups))
			.totalCount(popups.size())
			.build();
	}
}
