package com.motd.be.module.admin.popup.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.motd.be.module.admin.popup.repository.PopupAdminRepository;
import com.motd.be.module.member.popup.entity.Popup;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PopupAdminCommandService {

	private final PopupAdminRepository popupAdminRepository;

	public Popup save(Popup popup) {
		return popupAdminRepository.save(popup);
	}

	public void incrementSortOrder(Long id, int sortOrder) {
		popupAdminRepository.incrementSortOrder(id, sortOrder, LocalDateTime.now());
	}

	public void incrementSortOrderWithStartAndEnd(int start, int end) {
		popupAdminRepository.incrementSortOrderWithStartAndEnd(start, end, LocalDateTime.now());
	}

	public void decrementSortOrderWithStartAndEnd(int start, int end) {
		popupAdminRepository.decrementSortOrderWithStartAndEnd(start, end, LocalDateTime.now());
	}

	public void decrementSortOrder(Popup popup) {
		popupAdminRepository.decrementSortOrder(popup.getSortOrder(), LocalDateTime.now());
	}
}
