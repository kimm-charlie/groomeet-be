package com.motd.be.module.member.chat_room.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.member.chat_room.dto.response.ChatRoomFindAllResponse;
import com.motd.be.module.member.chat_room.dto.response.ChatRoomFindChatRoomServicesResponse;
import com.motd.be.module.member.chat_room.dto.response.ChatRoomFindDetailResponse;
import com.motd.be.module.member.chat_room.dto.response.ChatRoomUnreadCountResponse;
import com.motd.be.module.member.chat_room.facade.ChatRoomFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatRoomController {

	private final ChatRoomFacade chatRoomFacade;

	@PreAuthorize("hasAnyRole('MEMBER', 'DIRECTOR')")
	@GetMapping("/chat-rooms")
	public ResponseEntity<ChatRoomFindAllResponse> findAll(@AuthenticationPrincipal Long memberId,
		@RequestParam(value = DIRECTOR_SERVICE_ID, required = false) Long directorServiceId,
		@RequestParam(value = SHOW_ONLY_UNREAD, defaultValue = "false") boolean showOnlyUnread,
		@RequestParam(value = STATUS, required = false) String status,
		@RequestParam(value = WORD, required = false) String word,
		@RequestParam(value = PAGE, defaultValue = ZERO) int page) {

		return ResponseEntity.status(HttpStatus.OK)
			.body(chatRoomFacade.findAllForPublic(memberId, directorServiceId, showOnlyUnread, word, status, page));
	}

	@PreAuthorize("hasAnyRole('MEMBER', 'DIRECTOR')")
	@GetMapping("/chat-rooms/services")
	public ResponseEntity<ChatRoomFindChatRoomServicesResponse> findChatRoomServices(
		@AuthenticationPrincipal Long memberId) {
		return ResponseEntity.status(HttpStatus.OK).body(chatRoomFacade.findChatRoomServicesForPublic(memberId));
	}

	@PreAuthorize("hasAnyRole('MEMBER', 'DIRECTOR')")
	@GetMapping("/chat-rooms/unread-count")
	public ResponseEntity<ChatRoomUnreadCountResponse> findTotalUnreadCount(@AuthenticationPrincipal Long memberId) {
		return ResponseEntity.status(HttpStatus.OK).body(chatRoomFacade.countTotalUnreadMessagesForPublic(memberId));
	}

	@PreAuthorize("hasAnyRole('MEMBER', 'DIRECTOR')")
	@GetMapping("/chat-rooms/{chatRoomId}")
	public ResponseEntity<ChatRoomFindDetailResponse> findDetail(@AuthenticationPrincipal Long memberId,
		@RequestParam(value = LAST_MESSAGE_ID, required = false) Long lastMessageId,
		@PathVariable(value = CHAT_ROOM_ID) Long chatRoomId) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(chatRoomFacade.findDetail(lastMessageId, memberId, chatRoomId));
	}

	@PreAuthorize("hasAnyRole('MEMBER', 'DIRECTOR')")
	@DeleteMapping("/chat-rooms/{chatRoomId}")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal Long memberId,
		@PathVariable(CHAT_ROOM_ID) Long chatRoomId) {
		chatRoomFacade.delete(memberId, chatRoomId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

}
