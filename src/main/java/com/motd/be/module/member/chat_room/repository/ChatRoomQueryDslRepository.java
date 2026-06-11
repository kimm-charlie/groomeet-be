package com.motd.be.module.member.chat_room.repository;

import static com.motd.be.module.member.chat_room.entity.QChatRoom.*;
import static com.motd.be.module.member.chat_room_member.entity.QChatRoomMember.*;
import static com.motd.be.module.member.chat_room_service_estimate_mapping.entity.QChatRoomServiceEstimateMapping.*;
import static com.motd.be.module.member.director_info.entity.QDirectorInfo.*;
import static com.motd.be.module.member.member.entity.QMember.*;
import static com.motd.be.module.member.service_estimate.entity.QServiceEstimate.*;
import static com.motd.be.module.member.service_request.entity.QServiceRequest.*;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ChatRoomQueryDslRepository {

	private final JPAQueryFactory query;

	public Slice<ChatRoom> findAllForPublic(
		Member member,
		Long directorServiceId,
		boolean showOnlyUnread,
		String word,
		String status,
		Pageable pageable
	) {

		JPAQuery<ChatRoom> query = this.query
			.select(chatRoom)
			.from(chatRoom)
			.join(chatRoom.chatRoomMembers, chatRoomMember)
			.where(
				chatRoom.isDeleted.isFalse(),
				chatRoomMember.member.eq(member),
				chatRoomMember.isDirector.isFalse(),
				chatRoomMember.isChatRoomDeleted.isFalse(),
				existsServiceEstimateForPublic(directorServiceId),
				existsStatusForPublic(status),
				existsUnreadForPublic(showOnlyUnread),
				existsWordForPublic(word)
			)
			.orderBy(chatRoomMember.lastVisibleMessage.sendAt.desc(), chatRoomMember.lastVisibleMessage.id.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1);

		List<ChatRoom> results = query.fetch();

		boolean hasNext = results.size() > pageable.getPageSize();
		if (hasNext) {
			results.remove(results.size() - 1);
		}

		return new SliceImpl<>(results, pageable, hasNext);
	}

	private BooleanExpression existsServiceEstimateForPublic(Long directorServiceId) {
		if (directorServiceId == null)
			return null;

		return JPAExpressions
			.selectOne()
			.from(chatRoomServiceEstimateMapping)
			.join(chatRoomServiceEstimateMapping.serviceEstimate, serviceEstimate)
			.join(serviceEstimate.serviceRequest, serviceRequest)
			.where(
				chatRoomServiceEstimateMapping.chatRoom.eq(chatRoom),
				serviceRequest.directorService.id.eq(directorServiceId)
			)
			.exists();
	}

	private BooleanExpression existsStatusForPublic(String status) {
		if (status == null || status.isBlank())
			return null;

		ServiceRequestStatus requestStatus = ServiceRequestStatus.from(status);

		return JPAExpressions
			.selectOne()
			.from(chatRoomServiceEstimateMapping)
			.join(chatRoomServiceEstimateMapping.serviceEstimate, serviceEstimate)
			.join(serviceEstimate.serviceRequest, serviceRequest)
			.where(
				chatRoomServiceEstimateMapping.chatRoom.eq(chatRoom),
				switch (requestStatus) {
					case PENDING -> serviceRequest.status.eq(ServiceRequestStatus.PENDING);
					case ONGOING -> serviceRequest.status.eq(ServiceRequestStatus.ONGOING);
					default -> null;
				}
			)
			.exists();
	}

	private BooleanExpression existsUnreadForPublic(boolean showOnlyUnread) {
		if (!showOnlyUnread)
			return null;

		return chatRoomMember.lastVisibleMessage.id.isNotNull()
			.and(
				chatRoomMember.lastReadMessage.id.isNull()
					.or(chatRoomMember.lastReadMessage.id.lt(chatRoomMember.lastVisibleMessage.id))
			);
	}

	private BooleanExpression existsWordForPublic(String word) {
		if (word == null || word.isBlank())
			return null;

		return JPAExpressions
			.selectOne()
			.from(chatRoomServiceEstimateMapping)
			.join(chatRoomServiceEstimateMapping.serviceEstimate, serviceEstimate)
			.join(serviceEstimate.directorInfo, directorInfo)
			.join(directorInfo.member, member)
			.join(serviceEstimate.serviceRequest, serviceRequest)
			.where(
				chatRoomServiceEstimateMapping.chatRoom.eq(chatRoom),
				member.nickname.contains(word)
					.or(serviceRequest.directorService.name.contains(word))
			)
			.exists();
	}

}
