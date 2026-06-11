package com.motd.be.module.member.chat_file.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.chat_file.entity.ChatFile;
import com.motd.be.module.member.chat_file.validator.ChatFileValidator;
import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.chat_message.service.ChatMessageCommandService;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.shared.aws.enums.UploadFileType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatFileService {

	private final ChatFileQueryService chatFileQueryService;
	private final ChatFileValidator chatFileValidator;
	private final ChatMessageCommandService chatMessageCommandService;

	public void deleteIfNeeded(ChatMessage chatMessage, Member member) {
		if (chatMessage.isMessageTypeFile()) {
			chatMessageCommandService.deleteChatFilesByChatMessageAndMember(chatMessage, member);
		}
	}

	public List<ChatFile> findAllByIdsAndValidate(List<Long> fileIds,
		Member member, UploadFileType uploadFileType) {
		// 이미지 조회
		List<ChatFile> chatFiles = chatFileQueryService.findAllByIds(fileIds);

		// 이미지 소유자 검증 및 파일 타입 검증
		chatFileValidator.validateFiles(member.getId(), chatFiles, uploadFileType);

		return chatFiles;
	}
}
