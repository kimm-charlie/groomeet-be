package com.motd.be.module.member.chat_file.validator;

import java.util.List;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ChatFileException;
import com.motd.be.module.member.chat_file.entity.ChatFile;
import com.motd.be.shared.aws.enums.UploadFileType;

@Component
public class ChatFileValidator {

	public void validateFiles(Long memberId, List<ChatFile> chatFiles, UploadFileType uploadFileType) {
		chatFiles.forEach(chatImage -> {

			// 이미지 소유권 검증
			if (!chatImage.getMember().getId().equals(memberId)) {
				throw new CustomRuntimeException(ChatFileException.NOT_OWNED);
			}

			// 이미지 파일 타입 검증
			if (!chatImage.getFileType().equals(uploadFileType)) {
				throw new CustomRuntimeException(ChatFileException.INVALID_FILE_TYPE);
			}
		});
	}
}
