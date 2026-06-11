package com.motd.be.module.member.popup.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.popup.entity.Popup;
import com.motd.be.module.member.popup.repository.PopupRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopupQueryService {

	private final PopupRepository popUpRepository;

	public List<Popup> findAll() {
		return popUpRepository.findAll(LocalDateTime.now());
	}
}
