package com.motd.be.provider.module.member;

import static com.motd.be.Constants.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.popup.entity.Popup;
import com.motd.be.module.member.popup.entity.PopupType;
import com.motd.be.module.member.popup.repository.PopupRepository;

@Component
public class PopupProvider {

	@Autowired
	private PopupRepository popUpRepository;

	private static Popup popupDummy(LocalDateTime startDate, LocalDateTime endDate, Boolean isDeleted, int sortOrder) {
		return Popup.builder()
			.title(TITLE_STR)
			.cdnThumbnailImageUrl(THUMBNAIL_IMAGE_URL)
			.linkUrl(LINK_URL)
			.type(PopupType.MEMBER)
			.startAt(startDate)
			.endAt(endDate)
			.isDeleted(isDeleted)
			.sortOrder(sortOrder)
			.build();
	}

	private static Popup popupDummyWithType(PopupType type, LocalDateTime startDate, LocalDateTime endDate,
		Boolean isDeleted, int sortOrder) {
		return Popup.builder()
			.title(TITLE_STR)
			.cdnThumbnailImageUrl(THUMBNAIL_IMAGE_URL)
			.linkUrl(LINK_URL)
			.type(type)
			.startAt(startDate)
			.endAt(endDate)
			.isDeleted(isDeleted)
			.sortOrder(sortOrder)
			.build();
	}

	public Popup save(LocalDateTime startDate, LocalDateTime endDate, Boolean isDeleted, int sortOrder) {
		return popUpRepository.save(popupDummy(startDate, endDate, isDeleted, sortOrder));
	}

	public Popup saveWithType(PopupType type, LocalDateTime startDate, LocalDateTime endDate, int sortOrder) {
		return popUpRepository.save(popupDummyWithType(type, startDate, endDate, Boolean.FALSE, sortOrder));
	}

	public Popup saveWithTypeAndIsDeletedTrue(PopupType type, LocalDateTime startDate, LocalDateTime endDate,
		int sortOrder) {
		return popUpRepository.save(popupDummyWithType(type, startDate, endDate, Boolean.TRUE, sortOrder));
	}

	public List<Popup> findAll() {
		return popUpRepository.findAll();
	}
}
