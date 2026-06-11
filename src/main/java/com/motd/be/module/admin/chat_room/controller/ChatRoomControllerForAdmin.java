package com.motd.be.module.admin.chat_room.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.admin.chat_room.dto.response.ChatRoomFindDetailResponseForAdmin;
import com.motd.be.module.admin.chat_room.facade.ChatRoomFacadeForAdmin;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class ChatRoomControllerForAdmin {

	private final ChatRoomFacadeForAdmin chatRoomFacadeForAdmin;

	@PreAuthorize("hasAnyRole('ADMIN')")
	@GetMapping("/chat-rooms")
	public ResponseEntity<ChatRoomFindDetailResponseForAdmin> findDetail(
		@RequestParam(value = SERVICE_ESTIMATE_ID) Long serviceEstimateId) {
		return ResponseEntity.ok(chatRoomFacadeForAdmin.findDetailByServiceEstimateId(serviceEstimateId));
	}
}
