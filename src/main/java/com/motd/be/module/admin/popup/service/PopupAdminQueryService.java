package com.motd.be.module.admin.popup.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.PopUpException;
import com.motd.be.module.admin.popup.repository.PopupAdminQueryDslRepository;
import com.motd.be.module.admin.popup.repository.PopupAdminRepository;
import com.motd.be.module.member.popup.entity.Popup;
import com.motd.be.module.member.popup.entity.PopupType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PopupAdminQueryService {

	private final PopupAdminRepository popupAdminRepository;
	private final PopupAdminQueryDslRepository popupAdminQueryDslRepository;

	public Popup findById(Long id) {
		return popupAdminRepository.findById(id)
			.orElseThrow(() -> new CustomRuntimeException(PopUpException.NOT_FOUND));
	}

	public Slice<Popup> findAll(Pageable pageable, Boolean showIsDeleted, PopupType type) {
		return popupAdminQueryDslRepository.findAll(pageable, showIsDeleted, type);
	}
}
