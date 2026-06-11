package com.motd.be.module.member.chat_stomp.handler;

import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.chat_room_member.validator.ChatRoomMemberValidator;
import com.motd.be.module.member.chat_stomp.StompCommandHandler;
import com.motd.be.module.member.chat_stomp.dto.request.StompContext;
import com.motd.be.redis.domain.repository.RedisChatRoomSubscribeRepository;
import com.motd.be.redis.domain.repository.RedisChatSessionInfoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscribeCommandHandler implements StompCommandHandler {

	private final RedisChatRoomSubscribeRepository redisChatRoomSubscribeRepository;
	private final ChatRoomMemberValidator chatRoomMemberValidator;
	private final RedisChatSessionInfoRepository redisChatSessionInfoRepository;

	@Override
	public boolean supports(StompCommand command) {
		return command.equals(StompCommand.SUBSCRIBE);
	}

	@Override
	public void handle(StompHeaderAccessor accessor) {
		String destination = accessor.getDestination();
		if (destination == null)
			return;

		// /sub/chatRoom/{id} 형태만 검증
		if (destination.startsWith("/sub/chatRoom")) {
			StompContext ctx = StompContext.from(accessor);

			// 1. 접근 가능한 채팅방인지 검증
			chatRoomMemberValidator.validateMemberInChatRoom(ctx.getMemberId(), ctx.getChatRoomId());

			// 2. Redis에 구독 정보 저장
			redisChatRoomSubscribeRepository.subscribe(ctx.getChatRoomId(), ctx.getMemberId(), accessor.getSessionId());

			// 3. redis 에 session 기반 구독 정보 저장
			redisChatSessionInfoRepository.saveSessionInfo(ctx.getSessionId(), ctx.getChatRoomId(), ctx.getMemberId());
		} else {
			// /user/queue/errors 등은 스킵
			log.debug("SUBSCRIBE destination={} subscriptionId={} sessionId={}",
				accessor.getDestination(),
				accessor.getSubscriptionId(),
				accessor.getSessionId());
		}
	}
}
