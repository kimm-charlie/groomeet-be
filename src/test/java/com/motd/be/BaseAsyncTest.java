package com.motd.be;

import static com.motd.be.Constants.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.motd.be.module.member.chat_stomp.ErrorFrameHandler;
import com.motd.be.module.member.chat_stomp.SimpleFrameHandler;
import com.motd.be.module.member.sse.service.SseService;
import com.motd.be.provider.module.member.ChatMessageProvider;
import com.motd.be.provider.module.member.ChatRoomMemberProvider;
import com.motd.be.provider.module.member.ChatRoomProvider;
import com.motd.be.provider.module.member.ChatRoomServiceEstimateMappingProvider;
import com.motd.be.provider.module.member.DirectorInfoProvider;
import com.motd.be.provider.module.member.DirectorServiceProvider;
import com.motd.be.provider.module.member.MemberBlockProvider;
import com.motd.be.provider.module.member.MemberProvider;
import com.motd.be.provider.module.member.ServiceEstimateProvider;
import com.motd.be.provider.module.member.ServiceRequestProvider;
import com.motd.be.provider.redis.domain.RedisChatRoomSubscribeProvider;
import com.motd.be.provider.redis.domain.RedisChatSessionInfoProvider;
import com.motd.be.redis.domain.brocker.SseEventPublisher;
import com.motd.be.redis.domain.repository.RedisSseConnectionRepository;
import com.motd.be.shared.ai.provider.AiChatProvider;
import com.motd.be.shared.mobile_ok.service.MobileOkCryptoService;

import jakarta.persistence.EntityManager;

public abstract class BaseAsyncTest {

	@Autowired
	protected MockMvc mockMvc;
	@Autowired
	protected ObjectMapper objectMapper;
	@Autowired
	protected EntityManager entityManager;
	@Autowired
	protected MemberProvider memberProvider;
	@Autowired
	protected DirectorInfoProvider directorInfoProvider;
	@Autowired
	protected ChatRoomProvider chatRoomProvider;
	@Autowired
	protected ChatRoomMemberProvider chatRoomMemberProvider;
	@Autowired
	protected RedisChatRoomSubscribeProvider redisChatRoomSubscribeProvider;
	@Autowired
	protected ChatMessageProvider chatMessageProvider;
	@Autowired
	protected SseEventPublisher sseEventPublisher;
	@Autowired
	protected RedisSseConnectionRepository redisSseConnectionRepository;
	@MockitoSpyBean
	protected SseService sseService;
	@LocalServerPort
	protected int port;
	protected WebSocketStompClient stompClient;
	protected StompSession session;
	@Autowired
	protected DirectorServiceProvider directorServiceProvider;
	@Autowired
	protected ServiceRequestProvider serviceRequestProvider;
	@Autowired
	protected ServiceEstimateProvider serviceEstimateProvider;
	@Autowired
	protected ChatRoomServiceEstimateMappingProvider chatRoomServiceEstimateMappingProvider;
	@Autowired
	protected MemberBlockProvider memberBlockProvider;
	@Autowired
	protected RedisChatSessionInfoProvider redisChatSessionInfoProvider;
	@Autowired
	private CleanUpDb cleanUpDb;
	@MockitoBean
	private MobileOkCryptoService mobileOkCryptoService;
	@MockitoBean
	protected AiChatProvider aiChatProvider;

	@BeforeEach
	void setup() {
		cleanUpDb.all();

		stompClient = new WebSocketStompClient(new StandardWebSocketClient());
		stompClient.setMessageConverter(new MappingJackson2MessageConverter());
	}

	@AfterEach
	void teardown() {
		cleanUpDb.all();

		if (session != null && session.isConnected()) {
			session.disconnect();
		}
	}

	protected StompSession connectWithCookies(String accessToken, String refreshToken, BlockingQueue<String> errorQueue)
		throws ExecutionException, InterruptedException, TimeoutException {

		WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
		handshakeHeaders.add(COOKIE_STR, "accessToken=" + accessToken + "; refreshToken=" + refreshToken);

		return stompClient.connectAsync(
				"ws://localhost:" + port + "/ws",
				handshakeHeaders,
				new ErrorFrameHandler(errorQueue)
			)
			.get(5, TimeUnit.SECONDS);
	}

	protected StompHeaders buildSubscribeChatRoomHeaders(Long chatRoomId) {
		StompHeaders headers = new StompHeaders();
		headers.setDestination("/sub/chatRoom/" + chatRoomId);
		headers.add(CHAT_ROOM_ID_STR, String.valueOf(chatRoomId));
		return headers;
	}

	protected StompHeaders buildSubscribePingPongHeadersForSubscribe() {
		StompHeaders headers = new StompHeaders();
		headers.setDestination("/user/queue/ping");
		return headers;
	}

	protected StompHeaders buildSubscribePingPongHeadersForSend(Long chatRoomId) {
		StompHeaders headers = new StompHeaders();
		headers.add(CHAT_ROOM_ID_STR, String.valueOf(chatRoomId));
		headers.setDestination("/pub/pong");
		return headers;
	}

	protected StompHeaders buildSubscribeErrorHeaders(BlockingQueue<String> errorQueue) {
		StompHeaders errorHeaders = new StompHeaders();
		errorHeaders.setDestination("/user/queue/errors");
		session.subscribe(errorHeaders, new SimpleFrameHandler(errorQueue));
		return errorHeaders;
	}

	protected StompHeaders buildDisconnectHeaders(Long chatRoomId) {
		StompHeaders headers = new StompHeaders();
		headers.add(CHAT_ROOM_ID_STR, String.valueOf(chatRoomId));
		return headers;
	}

}
