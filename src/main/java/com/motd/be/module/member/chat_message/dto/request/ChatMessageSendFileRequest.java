package com.motd.be.module.member.chat_message.dto.request;

import static com.motd.be.common.constants.ValidationConstants.*;
import static com.motd.be.common.constants.ValidationMessages.*;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class ChatMessageSendFileRequest {

	@NotNull(message = CHAT_ROOM_ID_REQUIRED)
	private Long chatRoomId;
	@NotEmpty(message = IMAGE_REQUIRED)
	@Size(max = CHAT_MESSAGE_IMAGE_MAX_COUNT, message = CHAT_MESSAGE_IMAGE_MAX_COUNT_MSG)
	private List<Long> fileIds;
	@NotBlank(message = FILE_TYPE_REQUIRED)
	private String fileType;

}
