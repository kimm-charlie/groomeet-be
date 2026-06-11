package com.motd.be.module.director.chat_room.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.director.chat_room.dto.response.ChatRoomFindAllResponseForDirector;
import com.motd.be.module.director.chat_room.dto.response.ChatRoomFindChatRoomServicesResponseForDirector;
import com.motd.be.module.director.chat_room.dto.response.ChatRoomUnreadCountResponseForDirector;
import com.motd.be.module.director.chat_room.facade.ChatRoomFacadeForDirector;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/directors")
public class ChatRoomControllerForDirector {

	private final ChatRoomFacadeForDirector chatRoomFacadeForDirector;

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@GetMapping("/chat-rooms")
	public ResponseEntity<ChatRoomFindAllResponseForDirector> findAll(@AuthenticationPrincipal Long directorId,
		@RequestParam(value = DIRECTOR_SERVICE_ID, required = false) Long directorServiceId,
		@RequestParam(value = SHOW_ONLY_UNREAD, defaultValue = "false") boolean showOnlyUnread,
		@RequestParam(value = STATUS, required = false) String status,
		@RequestParam(value = WORD, required = false) String word,
		@RequestParam(value = PAGE, defaultValue = ZERO) int page) {

		return ResponseEntity.status(HttpStatus.OK)
			.body(chatRoomFacadeForDirector.findAll(directorId, directorServiceId, showOnlyUnread, word, status, page));
	}

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@GetMapping("/chat-rooms/services")
	public ResponseEntity<ChatRoomFindChatRoomServicesResponseForDirector> findChatRoomServices(
		@AuthenticationPrincipal Long directorId) {
		return ResponseEntity.status(HttpStatus.OK).body(chatRoomFacadeForDirector.findChatRoomServices(directorId));
	}

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@GetMapping("/chat-rooms/unread-count")
	public ResponseEntity<ChatRoomUnreadCountResponseForDirector> findTotalUnreadCountForDirector(
		@AuthenticationPrincipal Long directorId) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(chatRoomFacadeForDirector.countTotalUnreadMessages(directorId));
	}

}
