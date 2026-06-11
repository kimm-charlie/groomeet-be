package com.motd.be.module.admin.popup.service;

import static com.motd.be.common.constants.PageSizeConstants.*;
import static com.motd.be.common.utils.DateFormatUtils.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.module.admin.popup.dto.request.PopupSaveRequestForAdmin;
import com.motd.be.module.admin.popup.dto.request.PopupUpdateRequestForAdmin;
import com.motd.be.module.admin.popup.dto.response.PopupAdminFindAllResponseForAdmin;
import com.motd.be.module.admin.popup.dto.response.PopupAdminResponseForAdmin;
import com.motd.be.module.admin.popup_file.service.PopupFileCommandServiceForAdmin;
import com.motd.be.module.member.popup.entity.Popup;
import com.motd.be.module.member.popup.entity.PopupType;
import com.motd.be.module.member.popup.validator.PopupValidator;
import com.motd.be.module.member.popup_file.entity.PopupFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PopupAdminService {

	private final PopupAdminQueryService popupAdminQueryService;
	private final PopupAdminCommandService popupAdminCommandService;
	private final PopupValidator popupValidator;
	private final PopupFileCommandServiceForAdmin popupFileCommandServiceForAdmin;

	public Popup save(PopupSaveRequestForAdmin request, PopupFile thumbnailFile) {

		//1. 일차적으로 request 안에 데이터가 유효한지 확인한다.
		popupValidator.validatePopupDateForSave(request.getStartAt(), request.getEndAt());

		popupValidator.validateThumbnailFileExists(thumbnailFile);

		Popup savedPopup = popupAdminCommandService.save(request.toEntity(thumbnailFile));

		// 저장된 팝업 순서 변경을 위한 코드
		popupAdminCommandService.incrementSortOrder(savedPopup.getId(), request.getSortOrder());
		return savedPopup;
	}

	public void delete(Popup popup) {
		//팝업 삭제시 기존 팝업 뒤에 있던 팝업 순서 1씩 감소 시키기 (한칸씩 앞으로 당기기 위해)
		popupAdminCommandService.decrementSortOrder(popup);

		popup.delete();

		// 파일 삭제
		PopupFile thumbnailFile = popup.getThumbnailFile();
		thumbnailFile.delete();
	}

	public PopupAdminFindAllResponseForAdmin findAll(int page, Boolean showIsDeleted, PopupType type) {
		Pageable pageable = PageRequest.of(page, POPUP_PAGE_SIZE);

		Slice<Popup> popupPage = popupAdminQueryService.findAll(pageable, showIsDeleted, type);

		return PopupAdminFindAllResponseForAdmin.from(popupPage);
	}

	public PopupAdminResponseForAdmin findPopupById(Long popupId) {
		Popup popup = popupAdminQueryService.findById(popupId);
		return PopupAdminResponseForAdmin.from(popup);
	}

	public Popup update(Long popupId, PopupUpdateRequestForAdmin request, PopupFile thumbnailFile) {
		//1. request 안의 데이터 검증
		popupValidator.validatePopupDateForSave(request.getStartAt(), request.getEndAt());

		//2. 팝업 조회
		Popup popup = popupAdminQueryService.findById(popupId);

		popupValidator.validateThumbnailFileExists(thumbnailFile);

		// 팝업 파일 업데이트
		popup.deleteFileIfNeeded(thumbnailFile);

		//3. 팝업의 다른 정보 업데이트
		popup.updateInfo(request.getTitle(), request.getLinkUrl(),
			parseToLocalDateTime(request.getStartAt()), parseToLocalDateTime(request.getEndAt()), thumbnailFile);

		//4. 순서 변경
		if (!popup.getSortOrder().equals(request.getSortOrder())) {
			updateOrder(popup, request.getSortOrder());
		}

		return popup;
	}

	public void updateOrder(Popup popup, int newOrder) {
		int originalOrder = popup.getSortOrder();

		if (newOrder < originalOrder) {
			// 앞으로 이동 → 중간 구간 이벤트들을 뒤로 밀기 (+1)
			popupAdminCommandService.incrementSortOrderWithStartAndEnd(newOrder, originalOrder - 1);
		} else if (newOrder > originalOrder) {
			// 뒤로 이동 → 중간 구간 이벤트들을 앞으로 당기기 (-1)
			popupAdminCommandService.decrementSortOrderWithStartAndEnd(originalOrder + 1, newOrder);
		}

		popup.updateSortOrder(newOrder);
	}
}

