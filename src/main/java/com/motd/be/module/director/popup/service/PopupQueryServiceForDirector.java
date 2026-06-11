package com.motd.be.module.director.popup.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.director.popup.repository.PopupRepositoryForDirector;
import com.motd.be.module.member.popup.entity.Popup;
import com.motd.be.module.member.popup.entity.PopupType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PopupQueryServiceForDirector {

	private final PopupRepositoryForDirector popupRepositoryForDirector;

	public List<Popup> findAllActive() {
		return popupRepositoryForDirector.findAllActive(LocalDateTime.now(), PopupType.DIRECTOR);
	}
}
