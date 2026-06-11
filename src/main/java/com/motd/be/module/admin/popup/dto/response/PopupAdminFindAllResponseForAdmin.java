package com.motd.be.module.admin.popup.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.motd.be.module.member.popup.entity.Popup;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PopupAdminFindAllResponseForAdmin {

	private List<PopupAdminResponseForAdmin> popups;
	private int page;
	private Boolean hasNext;

	public static PopupAdminFindAllResponseForAdmin from(Slice<Popup> popups) {
		return PopupAdminFindAllResponseForAdmin.builder()
			.page(popups.getNumber())
			.hasNext(popups.hasNext())
			.popups(PopupAdminResponseForAdmin.fromList(popups.getContent()))
			.build();
	}
}
