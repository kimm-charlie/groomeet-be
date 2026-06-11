package com.motd.be.module.member.chat_file.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.FileException;
import com.motd.be.module.member.chat_file.entity.ChatFile;
import com.motd.be.module.member.chat_file.repository.ChatFileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatFileQueryService {

	private final ChatFileRepository chatFileRepository;

	public List<ChatFile> findAllByIds(List<Long> fileIds) {
		return chatFileRepository.findAllByIds(fileIds);
	}

	public ChatFile findByFileKey(String fileKey) {
		return chatFileRepository.findByFileKey(fileKey)
			.orElseThrow(() -> new CustomRuntimeException(FileException.FILE_NOT_FOUND));
	}
}
