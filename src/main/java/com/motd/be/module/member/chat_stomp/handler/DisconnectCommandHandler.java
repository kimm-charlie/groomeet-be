package com.motd.be.module.member.chat_stomp.handler;

import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.chat_stomp.StompCommandHandler;
import com.motd.be.module.member.chat_stomp.dto.request.StompContext;
import com.motd.be.redis.domain.repository.RedisChatRoomSubscribeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DisconnectCommandHandler implements StompCommandHandler {

	private final RedisChatRoomSubscribeRepository redisChatRoomSubscribeRepository;

	@Override
	public boolean supports(StompCommand command) {
		return command.equals(StompCommand.DISCONNECT);
	}

	@Override
	public void handle(StompHeaderAccessor accessor) {
		StompContext ctx = StompContext.from(accessor);

		// redis 에서 구독 정보 삭제
		redisChatRoomSubscribeRepository.unsubscribe(ctx.getChatRoomId(), ctx.getMemberId(), accessor.getSessionId());

		log.info(
			"DISCONNECT ByHandler!!!!!! STOMP frame [{}] from chatRoomId:{},  memberId={}, role={}, destination: {}",
			accessor.getCommand(), ctx.getChatRoomId(), ctx.getMemberId(), ctx.getRole(), accessor.getDestination());
	}
}
