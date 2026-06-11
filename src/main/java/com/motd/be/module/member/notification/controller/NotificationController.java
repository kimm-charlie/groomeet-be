package com.motd.be.module.member.notification.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.member.notification.dto.response.NotificationExistResponse;
import com.motd.be.module.member.notification.dto.response.NotificationFindAllResponse;
import com.motd.be.module.member.notification.facade.NotificationFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NotificationController {

	private final NotificationFacade notificationFacade;

	/**
	 * 알림 전체 조회
	 * - 일반 유저용
	 *
	 * @param memberId 인증된 사용자 ID
	 * @param page     페이지 번호
	 * @return 알림 목록과 읽지 않은 알림 수
	 */
	@PreAuthorize("hasAnyRole('MEMBER', 'DIRECTOR')")
	@GetMapping("/notifications")
	public ResponseEntity<NotificationFindAllResponse> findAll(
		@AuthenticationPrincipal Long memberId,
		@RequestParam(name = PAGE, required = false, defaultValue = ZERO) int page,
		@RequestParam(name = NOTIFICATION_CATEGORY_TYPE, required = false) String notificationCategoryType) {
		return ResponseEntity.ok(notificationFacade.findAllForPublic(memberId, page, notificationCategoryType));
	}

	@PreAuthorize("hasAnyRole('MEMBER', 'DIRECTOR')")
	@GetMapping("/notifications/exists")
	public ResponseEntity<NotificationExistResponse> hasUnreadNotification(@AuthenticationPrincipal Long memberId) {
		return ResponseEntity.ok(notificationFacade.hasUnreadForPublic(memberId));
	}
}
