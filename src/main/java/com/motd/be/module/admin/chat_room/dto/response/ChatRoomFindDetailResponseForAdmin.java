package com.motd.be.module.admin.chat_room.dto.response;

import com.motd.be.common.utils.DateFormatUtils;
import com.motd.be.module.admin.member.dto.response.MemberWithProfileImageResponseForAdmin;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomFindDetailResponseForAdmin {

	private Long chatRoomId;
	private Long serviceEstimateId;
	private MemberWithProfileImageResponseForAdmin director;
	private MemberWithProfileImageResponseForAdmin member;
	private Boolean isDirectorPaid;
	private String createdAt;

	public static ChatRoomFindDetailResponseForAdmin of(ChatRoom chatRoom, ServiceEstimate serviceEstimate) {
		MemberWithProfileImageResponseForAdmin director = null;
		MemberWithProfileImageResponseForAdmin member = null;

		for (ChatRoomMember chatRoomMember : chatRoom.getChatRoomMembers()) {
			if (chatRoomMember.getIsDirector()) {
				director = MemberWithProfileImageResponseForAdmin.from(chatRoomMember.getMember());
			} else {
				member = MemberWithProfileImageResponseForAdmin.from(chatRoomMember.getMember());
			}
		}

		return ChatRoomFindDetailResponseForAdmin.builder()
			.chatRoomId(chatRoom.getId())
			.serviceEstimateId(serviceEstimate.getId())
			.director(director)
			.member(member)
			.isDirectorPaid(chatRoom.getIsDirectorPaid())
			.createdAt(DateFormatUtils.formatToDateString(chatRoom.getCreatedAt()))
			.build();
	}
}
