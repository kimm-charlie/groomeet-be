package com.motd.be.provider.module.member;

import static com.motd.be.Constants.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.chat_file.entity.ChatFile;
import com.motd.be.module.member.chat_file.repository.ChatFileRepository;
import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.shared.aws.enums.UploadFileType;

@Component
public class ChatFileProvider {

	@Autowired
	private ChatFileRepository chatFileRepository;

	public ChatFile save(Member member) {
		return chatFileRepository.save(ChatFile.builder()
			.originUrl(ORIGIN_URL_STR)
			.cdnUrl(CDN_URL_STR)
			.fileKey(FILE_KEY_STR)
			.member(member)
			.sortOrder(0)
			.fileType(UploadFileType.IMAGE)
			.build());
	}

	public List<ChatFile> findAll() {
		return chatFileRepository.findAll();
	}

	public ChatFile saveWithChatMessage(Member director, ChatMessage chatMessage) {
		return chatFileRepository.save(ChatFile.builder()
			.originUrl(ORIGIN_URL_STR)
			.cdnUrl(CDN_URL_STR)
			.fileKey(FILE_KEY_STR)
			.member(director)
			.chatMessage(chatMessage)
			.sortOrder(0)
			.fileType(UploadFileType.IMAGE)
			.build());
	}
}
