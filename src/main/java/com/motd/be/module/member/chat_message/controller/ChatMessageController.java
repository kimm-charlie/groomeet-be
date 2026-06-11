package com.motd.be.module.member.chat_message.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.member.chat_message.dto.request.ChatMessageSendFileRequest;
import com.motd.be.module.member.chat_message.dto.response.ChatMessageFindAllResponse;
import com.motd.be.module.member.chat_message.facade.ChatMessageFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatMessageController {

	private final ChatMessageFacade chatMessageFacade;

	@PreAuthorize("hasAnyRole('MEMBER', 'DIRECTOR')")
	@GetMapping("/chat-messages")
	public ResponseEntity<ChatMessageFindAllResponse> findAll(@AuthenticationPrincipal Long memberId,
		@RequestParam(value = CHAT_ROOM_ID) Long chatRoomId,
		@RequestParam(value = LAST_MESSAGE_ID, required = false) Long lastMessageId) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(chatMessageFacade.findAllByChatRoomId(memberId, chatRoomId, lastMessageId));
	}

	@PreAuthorize("hasAnyRole('MEMBER', 'DIRECTOR')")
	@PostMapping("/chat-messages/files")
	public ResponseEntity<Void> sendFileMessage(
		@AuthenticationPrincipal Long memberId, @RequestBody @Validated ChatMessageSendFileRequest request) {
		chatMessageFacade.sendFileMessage(memberId, request);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PreAuthorize("hasAnyRole('MEMBER', 'DIRECTOR')")
	@DeleteMapping("/chat-messages/{chatMessageId}")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal Long memberId,
		@PathVariable(CHAT_MESSAGE_ID) Long chatMessageId) {
		chatMessageFacade.delete(memberId, chatMessageId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

}
