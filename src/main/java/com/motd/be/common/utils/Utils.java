package com.motd.be.common.utils;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.constants.TimePolicy.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.function.Function;

import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.member_metadata.entity.DeviceType;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_request.entity.ServiceRequest;

public class Utils {

	public static String generateMemberActiveUniqueKey(SignInPlatform signInPlatform, String identifier) {
		return signInPlatform.name() + "_" + identifier;
	}

	public static String generateBridgeCode() {
		return BRIDGE + "_" + UUID.randomUUID();
	}

	public static String generateMemberMetadataUniqueKey(Long memberId, DeviceType deviceType) {
		return memberId + "_" + deviceType.name();
	}

	public static String generateDirectorInfoLocationMappingUniqueKey(DirectorInfo directorInfo, Location location) {
		return directorInfo.getId() + "_" + location.getId();
	}

	public static String generateMemberLocationMappingUniqueKey(Member member, Location location) {
		return member.getId() + "_" + location.getId();
	}

	public static String generateDirectorInfoDirectorServiceMappingUniqueKey(DirectorInfo directorInfo,
		DirectorService directorService) {
		return directorInfo.getId() + "_" + directorService.getId();
	}

	public static String generateServiceEstimateUniqueKey(DirectorInfo directorInfo,
		ServiceRequest serviceRequest) {
		return directorInfo.getId() + "_" + serviceRequest.getId();
	}

	public static String generateChatRoomServiceEstimateMappingUniqueKey(ChatRoom chatRoom,
		ServiceEstimate serviceEstimate) {
		return chatRoom.getId() + "_" + serviceEstimate.getId();
	}

	public static String formatPhoneNumber(String raw) {
		if (raw == null) {
			return "";
		}
		if (raw.length() != 11) {
			return raw;
		}
		return raw.replaceFirst("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
	}

	/**
	 * 010xxxxxxxx -> +8210xxxxxxxx
	 * ex) 01085831507 -> +821085831507
	 */
	public static String toE164(String phoneNumber) {
		return "+82" + phoneNumber.substring(1);
	}

	public static <T> String buildFullName(
		T entity,
		Function<T, String> nameFn,
		Function<T, T> parentFn,
		String separator
	) {
		StringBuilder sb = new StringBuilder(nameFn.apply(entity));
		T parent = parentFn.apply(entity);

		while (parent != null) {
			sb.insert(0, nameFn.apply(parent) + separator);
			parent = parentFn.apply(parent);
		}

		return sb.toString().trim();
	}

	public static String normalizeCode(String rawCode) {
		if (rawCode == null) {
			return null;
		}

		return rawCode
			.replaceAll("\\s+", "") // 모든 공백 제거
			.toUpperCase();
	}

	public static LocalDate resolveOnboardingPassEndsAt() {
		LocalDate today = LocalDate.now();
		LocalDate calculated = today.plusMonths(DIRECTOR_ONBOARDING_PASS_FREE_MONTH);

		if (calculated.isBefore(ONBOARDING_ANCHOR_DATE)) {
			return ONBOARDING_ANCHOR_DATE;
		}

		return calculated;
	}

	public static LocalDateTime calculateReminderNeedAt(LocalDateTime scheduledAt) {
		return scheduledAt.minusDays(1).truncatedTo(ChronoUnit.HOURS);
	}
}
