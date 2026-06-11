package com.motd.be.module.member.member_metadata.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.MemberMetadataException;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.member_metadata.dto.request.MemberMetadataSaveOrUpdateRequest;
import com.motd.be.module.member.member_metadata.dto.response.MemberMetadataFindResponse;
import com.motd.be.module.member.member_metadata.entity.DeviceType;
import com.motd.be.module.member.member_metadata.entity.MemberMetadata;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class MemberMetadataControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("회원 메타데이터를 저장/업데이트할 수 있다. (저장)")
	void saveOrUpdateMemberMetadata() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		MemberMetadataSaveOrUpdateRequest request = MemberMetadataSaveOrUpdateRequest.builder()
			.deviceType(DeviceType.IOS.name())
			.version(VERSION)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/members/my/metadata")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk());

		entityManager.flush();
		entityManager.clear();

		// 실제로 저장되었는지 확인
		List<MemberMetadata> memberMetadataList = memberMetadataProvider.findAllByMember(member);

		assertThat(memberMetadataList).hasSize(1);

		MemberMetadata savedMetadata = memberMetadataList.get(0);
		assertThat(savedMetadata.getDeviceType()).isEqualTo(DeviceType.IOS);
		assertThat(savedMetadata.getVersion()).isEqualTo(VERSION);
	}

	@Test
	@DisplayName("회원 메타데이터를 저장/업데이트할 수 있다. (업데이트)")
	void saveOrUpdateMemberMetadata_Update() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		memberMetadataProvider.save(member, DeviceType.IOS, VERSION);

		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		String newVersion = VERSION + ".1";
		MemberMetadataSaveOrUpdateRequest request = MemberMetadataSaveOrUpdateRequest.builder()
			.deviceType(DeviceType.IOS.name())
			.version(newVersion)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/members/my/metadata")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk());

		entityManager.flush();
		entityManager.clear();

		// 실제로 저장되었는지 확인
		List<MemberMetadata> memberMetadataList = memberMetadataProvider.findAllByMember(member);

		assertThat(memberMetadataList).hasSize(1);

		MemberMetadata updatedMetadata = memberMetadataList.get(0);
		assertThat(updatedMetadata.getDeviceType()).isEqualTo(DeviceType.IOS);
		assertThat(updatedMetadata.getVersion()).isEqualTo(newVersion);
	}

	@Test
	@DisplayName("회원 메타데이터를 조회할 수 있다.")
	void find() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		MemberMetadata memberMetadata = memberMetadataProvider.save(member, DeviceType.IOS, VERSION);

		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		String json = mockMvc.perform(MockMvcRequestBuilders.get("/api/members/my/metadata")
				.param(DEVICE_TYPE_STR, DeviceType.IOS.name())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		entityManager.flush();
		entityManager.clear();

		// 실제로 조회되었는지 확인
		MemberMetadataFindResponse response = objectMapper.readValue(json, MemberMetadataFindResponse.class);

		assertThat(response.getId()).isEqualTo(memberMetadata.getId());
		assertThat(response.getVersion()).isEqualTo(VERSION);
	}

	@Test
	@DisplayName("회원 메타데이터를 조회할 수 있다. (없는 디바이스 타입 일때)")
	void findWhenDeviceTypeNotExist() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		MemberMetadata memberMetadata = memberMetadataProvider.save(member, DeviceType.IOS, VERSION);

		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.get("/api/members/my/metadata")
				.param(DEVICE_TYPE_STR, DeviceType.WEB.name())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(MemberMetadataException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(MemberMetadataException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(MemberMetadataException.NOT_FOUND.getCode()));
	}
}
