package com.motd.be.module.member.member_metadata.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member_metadata.dto.request.MemberMetadataSaveOrUpdateRequest;
import com.motd.be.module.member.member_metadata.dto.response.MemberMetadataFindResponse;
import com.motd.be.module.member.member_metadata.entity.DeviceType;
import com.motd.be.module.member.member_metadata.entity.MemberMetadata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberMetadataService {

	private final MemberMetadataCommandService memberMetadataCommandService;
	private final MemberMetadataQueryService memberMetadataQueryService;

	@Transactional
	public void saveOrUpdate(Member member, MemberMetadataSaveOrUpdateRequest request) {
		DeviceType deviceType = DeviceType.valueOf(request.getDeviceType());

		// 1.  회원의 기존 메타데이터 조회
		List<MemberMetadata> existingMetadata = memberMetadataCommandService.findAllByMember(member);

		// 2. 이미 존재하는 디바이스 타입이면 버전만 업데이트
		MemberMetadata existing = existingMetadata.stream()
			.filter(m -> m.getDeviceType().equals(deviceType))
			.findFirst()
			.orElse(null);

		if (existing != null) {
			existing.update(request.getVersion());
			return;
		}

		// 3. 없으면 새로 저장
		try {
			memberMetadataCommandService.save(request.toEntity(member, deviceType));
		} catch (DataIntegrityViolationException e) {
			// 중복 발생 시 무시
			log.warn("Duplicate deviceType for memberId {}: {}", member.getId(), e.getMessage());
		}

	}

	public MemberMetadataFindResponse find(Member member, String deviceType) {
		return MemberMetadataFindResponse.from(
			memberMetadataQueryService.findByMemberAndDeviceType(member, DeviceType.valueOf(deviceType)));
	}
}
