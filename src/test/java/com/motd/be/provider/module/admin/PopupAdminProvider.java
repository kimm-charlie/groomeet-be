package com.motd.be.provider.module.admin;

import static com.motd.be.Constants.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.admin.popup.repository.PopupAdminRepository;
import com.motd.be.module.member.popup.entity.Popup;
import com.motd.be.module.member.popup.entity.PopupType;
import com.motd.be.module.member.popup_file.entity.PopupFile;

@Component
public class PopupAdminProvider {

	@Autowired
	private PopupAdminRepository popupAdminRepository;

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

	private static Popup popupDummyWithThumbnail(LocalDateTime startDate, LocalDateTime endDate, Boolean isDeleted,
		int sortOrder, PopupFile thumbnailFile) {
		return Popup.builder()
			.title(TITLE_STR)
			.cdnThumbnailImageUrl(thumbnailFile.getCdnUrl())
			.thumbnailFile(thumbnailFile)
			.linkUrl(LINK_URL)
			.type(PopupType.MEMBER)
			.startAt(startDate)
			.endAt(endDate)
			.isDeleted(isDeleted)
			.sortOrder(sortOrder)
			.build();
	}

	public Popup save(LocalDateTime startDate, LocalDateTime endDate, Boolean isDeleted, int sortOrder) {
		return popupAdminRepository.save(popupDummy(startDate, endDate, isDeleted, sortOrder));
	}

	public Popup saveWithThumbnail(LocalDateTime startDate, LocalDateTime endDate, Boolean isDeleted, int sortOrder,
		PopupFile thumbnailFile) {
		return popupAdminRepository.save(popupDummyWithThumbnail(startDate, endDate, isDeleted, sortOrder, thumbnailFile));
	}

	public List<Popup> findAll() {
		return popupAdminRepository.findAll();
	}

	public Popup findById(Long id) {
		return popupAdminRepository.findById(id).orElseThrow();
	}
}
