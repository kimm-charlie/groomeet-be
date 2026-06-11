package com.motd.be.module.admin.chat_message.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.admin.chat_message.dto.response.ChatMessageFindAllResponseForAdmin;
import com.motd.be.module.admin.chat_room.facade.ChatRoomFacadeForAdmin;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class ChatMessageControllerForAdmin {

	private final ChatRoomFacadeForAdmin chatRoomFacadeForAdmin;

	@PreAuthorize("hasAnyRole('ADMIN')")
	@GetMapping("/chat-rooms/messages")
	public ResponseEntity<ChatMessageFindAllResponseForAdmin> findMessages(
		@RequestParam(value = SERVICE_ESTIMATE_ID) Long serviceEstimateId,
		@RequestParam(value = LAST_MESSAGE_ID, required = false) Long lastMessageId) {
		return ResponseEntity.ok(
			chatRoomFacadeForAdmin.findMessagesByServiceEstimateId(serviceEstimateId, lastMessageId));
	}
}
