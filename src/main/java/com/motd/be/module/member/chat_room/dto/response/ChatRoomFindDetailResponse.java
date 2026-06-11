package com.motd.be.module.member.chat_room.dto.response;

import org.springframework.data.domain.Slice;

import com.motd.be.module.member.chat_message.dto.response.ChatMessageFindAllResponse;
import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.member.dto.response.MemberResponse;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_estimate.dto.response.ServiceEstimateWithStatusResponse;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_request.dto.response.ServiceRequestForChatRoomFindDetailResponse;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomFindDetailResponse {

	private Long id;
	private MemberResponse opponent;
	private ServiceRequestForChatRoomFindDetailResponse request;
	private ServiceEstimateWithStatusResponse estimate;
	private ChatMessageFindAllResponse messageResponse;
	private Boolean isDirectorPaid;

	public static ChatRoomFindDetailResponse of(Member member, ChatRoom chatRoom, Slice<ChatMessage> chatMessages) {
		ServiceEstimate lastEstimate = chatRoom.getLatestEstimate();

		return ChatRoomFindDetailResponse.builder()
			.id(chatRoom.getId())
			.opponent(MemberResponse.from(chatRoom.getOtherMember(member)))
			.request(ServiceRequestForChatRoomFindDetailResponse.from(lastEstimate.getServiceRequest()))
			.messageResponse(ChatMessageFindAllResponse.from(chatMessages))
			.estimate(ServiceEstimateWithStatusResponse.from(lastEstimate))
			.isDirectorPaid(chatRoom.isDirectorPaid())
			.build();
	}
}
