package com.motd.be.module.member.prompt.facade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.director_service.service.DirectorServiceQueryService;
import com.motd.be.module.member.director_service.service.DirectorServiceService;
import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.location.service.LocationQueryService;
import com.motd.be.module.member.location.validator.LocationValidator;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.service.MemberQueryService;
import com.motd.be.module.member.prompt.dto.request.PromptGenerateRequest;
import com.motd.be.module.member.prompt.dto.request.PromptServiceRecommendRequest;
import com.motd.be.module.member.prompt.dto.response.PromptGenerateResponse;
import com.motd.be.module.member.prompt.dto.response.PromptRecommendResult;
import com.motd.be.module.member.prompt.dto.response.PromptServiceRecommendResponse;
import com.motd.be.module.member.prompt.dto.response.PromptServiceRecommendResponse.ServiceRecommendation;
import com.motd.be.module.member.prompt.entity.PromptMessage;
import com.motd.be.module.member.prompt.entity.PromptMessageRole;
import com.motd.be.module.member.prompt.entity.PromptRoom;
import com.motd.be.module.member.prompt.service.PromptMessageCommandService;
import com.motd.be.module.member.prompt.service.PromptRoomCommandService;
import com.motd.be.module.member.prompt.service.PromptService;
import com.motd.be.module.member.prompt.validator.PromptValidator;
import com.motd.be.module.member.service_request_file.service.ServiceRequestFileService;
import com.motd.be.shared.ai.dto.request.AiChatCompletionRequest.Message;
import com.motd.be.shared.ai.dto.response.AiRecommendResult;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromptFacade {

	private final DirectorServiceService directorServiceService;
	private final DirectorServiceQueryService directorServiceQueryService;
	private final PromptService promptService;
	private final PromptValidator promptValidator;
	private final ServiceRequestFileService serviceRequestFileService;
	private final LocationQueryService locationQueryService;
	private final LocationValidator locationValidator;
	private final MemberQueryService memberQueryService;
	private final PromptRoomCommandService promptRoomCommandService;
	private final PromptMessageCommandService promptMessageCommandService;

	@Transactional
	public PromptServiceRecommendResponse recommendServices(Long memberId, PromptServiceRecommendRequest request) {
		// 프롬프트 검증
		promptValidator.validatePrompt(request.getPrompt());

		// 대화방 준비 (roomId 없으면 새 대화방·이력 없음, 있으면 소유권·턴수 검증 후 이전 대화 이력 로딩)
		PromptRoom room;
		List<PromptMessage> history;
		if (request.getRoomId() == null) {
			Member member = memberQueryService.findById(memberId);
			room = promptRoomCommandService.save(PromptRoom.builder().member(member).build());
			history = List.of();
		} else {
			room = promptRoomCommandService.findById(request.getRoomId());
			promptValidator.validateRoomOwnership(room, memberId);
			promptValidator.validateMaxTurns(room);
			history = promptMessageCommandService.findAllByRoom(room);
		}

		// 이미지 CDN URL·추천 후보 서비스·대화 컨텍스트 준비
		List<String> cdnUrls = serviceRequestFileService.extractCdnUrls(memberId, request.getFileIds());
		List<DirectorService> activeServices = directorServiceQueryService.findAllActiveChildServices();
		List<Message> conversationHistory = buildConversationHistory(history, memberId);

		// AI 추천 호출 (구조화 JSON 응답 파싱 포함)
		PromptRecommendResult result = promptService.recommendServices(
			request.getPrompt(), activeServices, cdnUrls, conversationHistory);

		// 대화 턴 수 증가 + 사용자·AI 메시지 영속
		room.incrementTurnCount();
		saveMessages(room, request.getPrompt(), request.getFileIds(), result.getRawAiContent());

		// 매칭 결과 분기 (정보 부족 → 추가 질문 / 충분 → 추천 목록)
		AiRecommendResult parsedResult = result.getParsedResult();
		room.updateServiceRecommendSuccess(parsedResult.isMatched());

		if (!parsedResult.isMatched()) {
			return PromptServiceRecommendResponse.ofUnmatched(room.getId(), parsedResult.getMessage());
		}

		List<ServiceRecommendation> recommendations = parsedResult.getRecommendations().stream()
			.map(raw -> ServiceRecommendation.of(raw.getServiceId(), raw.getServiceName()))
			.toList();

		return PromptServiceRecommendResponse.ofMatched(room.getId(), recommendations);
	}

	@Transactional
	public PromptGenerateResponse generateRequest(Long memberId, PromptGenerateRequest request) {
		// 프롬프트 검증
		promptValidator.validatePrompt(request.getPrompt());

		// 디렉터 서비스 검증·조회
		DirectorService directorService = directorServiceService.validateAndFindForRequest(
			request.getDirectorServiceId());

		// 요청 위치 검증 (조합·개수·혼합 타입)
		List<Location> locations = locationQueryService.findAllByIds(request.getLocationIds());
		locationValidator.validateCombinationAndSize(locations, request.getLocationIds());
		locationValidator.validateForMixedType(locations);

		// 대화방 준비 (기존이면 소유권 검증·이력 로딩, 신규면 생성) + 디렉터 서비스 연결
		PromptRoom room;
		List<PromptMessage> history;
		if (request.getRoomId() != null) {
			room = promptRoomCommandService.findById(request.getRoomId());
			promptValidator.validateRoomOwnership(room, memberId);
			history = promptMessageCommandService.findAllByRoom(room);
			room.updateDirectorService(directorService);
		} else {
			Member member = memberQueryService.findById(memberId);
			room = promptRoomCommandService.save(PromptRoom.builder().member(member).build());
			room.updateDirectorService(directorService);
			history = List.of();
		}

		// 이미지 CDN URL·대화 컨텍스트 준비
		List<String> cdnUrls = serviceRequestFileService.extractCdnUrls(memberId, request.getFileIds());
		List<Message> conversationHistory = buildConversationHistory(history, memberId);

		// AI 요청서 생성 호출
		String aiContent = promptService.generateRequest(
			request.getPrompt(), directorService, cdnUrls, conversationHistory);

		// 대화 턴 수 증가 + 사용자·AI 메시지 영속
		room.incrementTurnCount();
		saveMessages(room, request.getPrompt(), request.getFileIds(), aiContent);

		return PromptGenerateResponse.from(room.getId(), aiContent);
	}

	private List<Message> buildConversationHistory(List<PromptMessage> history, Long memberId) {
		if (history.isEmpty()) {
			return List.of();
		}

		List<Message> messages = new ArrayList<>();
		for (PromptMessage msg : history) {
			if (msg.getRole() == PromptMessageRole.USER) {
				List<String> cdnUrls = extractCdnUrlsFromFileIds(msg.getFileIds(), memberId);
				messages.add(buildUserMessage(msg.getContent(), cdnUrls));
			} else {
				messages.add(Message.ofText("assistant", msg.getContent()));
			}
		}
		return messages;
	}

	private List<String> extractCdnUrlsFromFileIds(String fileIdsStr, Long memberId) {
		if (fileIdsStr == null || fileIdsStr.isBlank()) {
			return Collections.emptyList();
		}

		List<Long> fileIds = Arrays.stream(fileIdsStr.split(","))
			.map(String::strip)
			.map(Long::valueOf)
			.toList();

		return serviceRequestFileService.extractCdnUrls(memberId, fileIds);
	}

	private Message buildUserMessage(String content, List<String> cdnUrls) {
		if (cdnUrls == null || cdnUrls.isEmpty()) {
			return Message.ofText("user", content);
		}

		var contents = new ArrayList<com.motd.be.shared.ai.dto.request.AiChatCompletionRequest.Content>();
		contents.add(com.motd.be.shared.ai.dto.request.AiChatCompletionRequest.Content.ofText(content));
		for (String cdnUrl : cdnUrls) {
			contents.add(com.motd.be.shared.ai.dto.request.AiChatCompletionRequest.Content.ofImageUrl(cdnUrl));
		}

		return Message.ofMultiContent("user", contents);
	}

	private void saveMessages(PromptRoom room, String userPrompt, List<Long> fileIds, String aiContent) {
		String fileIdsStr = toFileIdsString(fileIds);

		promptMessageCommandService.save(PromptMessage.builder()
			.promptRoom(room)
			.role(PromptMessageRole.USER)
			.content(userPrompt)
			.fileIds(fileIdsStr)
			.build());

		promptMessageCommandService.save(PromptMessage.builder()
			.promptRoom(room)
			.role(PromptMessageRole.ASSISTANT)
			.content(aiContent)
			.build());
	}

	private String toFileIdsString(List<Long> fileIds) {
		if (fileIds == null || fileIds.isEmpty()) {
			return null;
		}
		return fileIds.stream()
			.map(String::valueOf)
			.collect(Collectors.joining(","));
	}
}
