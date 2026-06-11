package com.motd.be.module.member.popup.facade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.popup.dto.response.PopupFindAllResponse;
import com.motd.be.module.member.popup.service.PopupQueryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopupFacade {

	private final PopupQueryService popupQueryService;

	public PopupFindAllResponse findAll() {
		// 1. 팝업 전제 조회
		return PopupFindAllResponse.fromList(popupQueryService.findAll());
	}
}
