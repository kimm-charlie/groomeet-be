package com.motd.be.module.director.notification.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.director.notification.dto.response.NotificationExistResponseForDirector;
import com.motd.be.module.director.notification.dto.response.NotificationFindAllResponseForDirector;
import com.motd.be.module.director.notification.facade.NotificationFacadeForDirector;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/directors")
public class NotificationControllerForDirector {

	private final NotificationFacadeForDirector notificationFacade;

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@GetMapping("/notifications")
	public ResponseEntity<NotificationFindAllResponseForDirector> findAll(@AuthenticationPrincipal Long memberId,
		@RequestParam(name = PAGE, required = false, defaultValue = ZERO) int page,
		@RequestParam(name = NOTIFICATION_CATEGORY_TYPE, required = false) String notificationCategoryType) {
		return ResponseEntity.ok(notificationFacade.findAllForDirector(memberId, page, notificationCategoryType));
	}

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@GetMapping("/notifications/exists")
	public ResponseEntity<NotificationExistResponseForDirector> hasUnreadNotification(
		@AuthenticationPrincipal Long memberId) {
		return ResponseEntity.ok(notificationFacade.hasUnreadForDirector(memberId));
	}
}
