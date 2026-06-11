package com.motd.be.module.director.chat_room.dto.response;

import static com.motd.be.common.utils.DateFormatUtils.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.motd.be.module.director.director_service.dto.response.DirectorServiceWithFullNameResponseForDirector;
import com.motd.be.module.director.location.dto.response.LocationResponseForDirector;
import com.motd.be.module.director.member.dto.response.MemberResponseForDirector;
import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room_service_estimate_mapping.entity.ChatRoomServiceEstimateMapping;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.request_location_mapping.entity.RequestLocationMapping;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomResponseForDirector {

	private Long id;
	private String lastMessage;
	private MemberResponseForDirector opponent;
	private Integer unreadCount;
	private DirectorServiceWithFullNameResponseForDirector serviceResponse;
	private List<LocationResponseForDirector> locationResponse;
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
	public static List<ChatRoomResponseForDirector> ofList(List<ChatRoom> chatRooms, Map<Long, Integer> unreadCountMap,
		Member viewer) {
		return chatRooms.stream()
			.map(chatRoom -> {
				ChatMessage lastMessage = chatRoom.getChatRoomMember(viewer).getLastVisibleMessage();
				Integer unreadCount = unreadCountMap.getOrDefault(chatRoom.getId(), 0);
				ServiceEstimate serviceEstimate = chatRoom.getChatRoomServiceEstimateMappings().stream()
					.max(Comparator.comparing(ChatRoomServiceEstimateMapping::getCreatedAt))
					.get()
					.getServiceEstimate();
				return ChatRoomResponseForDirector.of(chatRoom, lastMessage, unreadCount,
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
	public static ChatRoomResponseForDirector of(ChatRoom chatRoom, ChatMessage lastMessage, Integer unreadCount,
		Member opponent, ServiceEstimate serviceEstimate) {

		return ChatRoomResponseForDirector.builder()
			.id(chatRoom.getId())
			.lastMessage(lastMessage != null ? lastMessage.getContent() : null)
			.lastMessageSendAt(lastMessage != null ? formatToDateString(lastMessage.getSendAt()) : null)
			.opponent(MemberResponseForDirector.from(opponent))
			.unreadCount(unreadCount)
			.serviceResponse(
				DirectorServiceWithFullNameResponseForDirector.from(
					serviceEstimate.getServiceRequest().getDirectorService()))
			.locationResponse(LocationResponseForDirector.fromList(
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
