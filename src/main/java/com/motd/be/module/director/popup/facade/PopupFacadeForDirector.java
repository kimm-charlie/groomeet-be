package com.motd.be.module.director.popup.facade;

import org.springframework.stereotype.Component;

import com.motd.be.module.director.popup.dto.response.PopupFindAllResponseForDirector;
import com.motd.be.module.director.popup.service.PopupQueryServiceForDirector;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PopupFacadeForDirector {

	private final PopupQueryServiceForDirector popupQueryServiceForDirector;

	public PopupFindAllResponseForDirector findAll() {
		return PopupFindAllResponseForDirector.from(popupQueryServiceForDirector.findAllActive());
	}
}
