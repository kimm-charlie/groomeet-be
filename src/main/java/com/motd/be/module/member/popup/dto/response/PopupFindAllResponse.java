package com.motd.be.module.member.popup.dto.response;

import java.util.List;

import com.motd.be.module.member.popup.entity.Popup;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PopupFindAllResponse {

	private List<PopupResponse> popUps;
	private Integer totalCount;

	public static PopupFindAllResponse fromList(List<Popup> popUps) {
		return PopupFindAllResponse.builder()
			.popUps(popUps.stream()
				.map(PopupResponse::from)
				.toList())
			.totalCount(popUps.size())
			.build();
	}

}
