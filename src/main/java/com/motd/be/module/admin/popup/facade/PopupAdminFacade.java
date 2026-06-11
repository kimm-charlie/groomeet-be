package com.motd.be.module.admin.popup.facade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.admin.popup.dto.request.PopupSaveRequestForAdmin;
import com.motd.be.module.admin.popup.dto.request.PopupUpdateRequestForAdmin;
import com.motd.be.module.admin.popup.dto.response.PopupAdminFindAllResponseForAdmin;
import com.motd.be.module.admin.popup.dto.response.PopupAdminResponseForAdmin;
import com.motd.be.module.admin.popup.service.PopupAdminQueryService;
import com.motd.be.module.admin.popup.service.PopupAdminService;
import com.motd.be.module.admin.popup_file.service.PopupFileQueryServiceForAdmin;
import com.motd.be.module.member.popup.entity.Popup;
import com.motd.be.module.member.popup.entity.PopupType;
import com.motd.be.module.member.popup_file.entity.PopupFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopupAdminFacade {

	private final PopupAdminService popupAdminService;
	private final PopupAdminQueryService popupAdminQueryService;
	private final PopupFileQueryServiceForAdmin popupFileQueryServiceForAdmin;

	@Transactional
	public PopupAdminResponseForAdmin save(PopupSaveRequestForAdmin request) {
		PopupFile thumbnailFile = popupFileQueryServiceForAdmin.findById(request.getThumbnailFileId());

		Popup popup = popupAdminService.save(request, thumbnailFile);

		return PopupAdminResponseForAdmin.from(popup);
	}

	@Transactional
	public void delete(Long popupId) {
		// 팝업 조회
		Popup popup = popupAdminQueryService.findById(popupId);

		popupAdminService.delete(popup);
	}

	@Transactional
	public void update(Long popupId, PopupUpdateRequestForAdmin request) {
		PopupFile thumbnailFile = popupFileQueryServiceForAdmin.findById(request.getThumbnailFileId());

		popupAdminService.update(popupId, request, thumbnailFile);
	}

	public PopupAdminFindAllResponseForAdmin findAll(int page, Boolean showIsDeleted, PopupType type) {
		return popupAdminService.findAll(page, showIsDeleted, type);
	}

	public PopupAdminResponseForAdmin findPopupById(Long popupId) {
		return popupAdminService.findPopupById(popupId);
	}
}
