package com.motd.be.module.member.chat_room.dto.response;

import static com.motd.be.common.utils.DateFormatUtils.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room_service_estimate_mapping.entity.ChatRoomServiceEstimateMapping;
import com.motd.be.module.member.director_service.dto.response.DirectorServiceWithFullNameResponse;
import com.motd.be.module.member.location.dto.response.LocationResponse;
import com.motd.be.module.member.member.dto.response.MemberResponse;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.request_location_mapping.entity.RequestLocationMapping;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomResponse {

	private Long id;
	private String lastMessage;
	private MemberResponse opponent;
	private Integer unreadCount;
	private DirectorServiceWithFullNameResponse serviceResponse;
	private List<LocationResponse> locationResponse;
	private Long price;
	private String lastMessageSendAt;
	private Boolean isHired;
	private Boolean isInTransaction;

	/**
	 * 채팅방 전체조회용 DTO 생성 메서드
	 *
	 * @param chatRooms
	 * @param unreadCountMap
	 * @param viewer
	 * @return
	 */
	public static List<ChatRoomResponse> ofList(List<ChatRoom> chatRooms, Map<Long, Integer> unreadCountMap,
		Member viewer) {
		return chatRooms.stream()
			.map(chatRoom -> {
				ChatMessage lastMessage = chatRoom.getChatRoomMember(viewer).getLastVisibleMessage();
				Integer unreadCount = unreadCountMap.getOrDefault(chatRoom.getId(), 0);
				ServiceEstimate serviceEstimate = chatRoom.getChatRoomServiceEstimateMappings().stream()
					.max(Comparator.comparing(ChatRoomServiceEstimateMapping::getCreatedAt))
					.get()
					.getServiceEstimate();
				return ChatRoomResponse.of(chatRoom, lastMessage, unreadCount,
					chatRoom.getOtherMember(viewer), serviceEstimate);
			})
			.toList();
	}

	/**
	 * sse 전용 DTO 생성 메서드
	 *
	 * @param chatRoom
	 * @param lastMessage
	 * @param unreadCount
	 * @param opponent
	 * @param serviceEstimate
	 * @return
	 */
	public static ChatRoomResponse of(ChatRoom chatRoom, ChatMessage lastMessage, Integer unreadCount, Member opponent,
		ServiceEstimate serviceEstimate) {

		return ChatRoomResponse.builder()
			.id(chatRoom.getId())
			.lastMessage(lastMessage != null ? lastMessage.getContent() : null)
			.lastMessageSendAt(lastMessage != null ? formatToDateString(lastMessage.getSendAt()) : null)
			.opponent(MemberResponse.from(opponent))
			.unreadCount(unreadCount)
			.serviceResponse(
				DirectorServiceWithFullNameResponse.from(serviceEstimate.getServiceRequest().getDirectorService()))
			.locationResponse(LocationResponse.fromList(
				serviceEstimate.getServiceRequest().getRequestLocationMappings().stream()
					.map(RequestLocationMapping::getLocation)
					.toList()
			))
			.price(serviceEstimate.getPrice())
			.isHired(serviceEstimate.getIsHired())
			.isInTransaction(serviceEstimate.getIsInTransaction())
			.build();
	}
}
