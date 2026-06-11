package com.motd.be.module.member.time_slot.controller;

import static com.motd.be.Constants.ERROR_CODE;
import static com.motd.be.Constants.ERROR_MESSAGE;
import static com.motd.be.Constants.ERROR_STATUS;
import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.utils.DateFormatUtils.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.TimeSlotException;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.time_slot.dto.response.TimeSlotFindAllResponse;
import com.motd.be.module.member.time_slot.dto.response.TimeSlotResponse;
import com.motd.be.module.member.time_slot.entity.TimeSlot;

@ControllerIntegrationTest
public class TimeSlotControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("미래 날짜로 시간 슬롯 전체 조회 시 24개 슬롯이 모두 selectable로 반환된다")
	void findAll() throws Exception {
		// when
		MvcResult result = mockMvc.perform(get("/api/time-slots")
				.param(DATE_STR, formatToDateString(LocalDate.now().plusDays(1))))
			.andExpect(status().isOk())
			.andReturn();

		// then
		String content = result.getResponse().getContentAsString();
		TimeSlotFindAllResponse response = objectMapper.readValue(content, TimeSlotFindAllResponse.class);

		assertThat(response.getSlots()).hasSize(24);
		assertThat(response.getSlots()).allMatch(TimeSlotResponse::isSelectable);
		assertThat(response.getSlots().get(0).getTime()).isEqualTo(formatToTimeString(TimeSlot.SLOT_09_00.getTime()));
		assertThat(response.getSlots().get(23).getTime()).isEqualTo(formatToTimeString(TimeSlot.SLOT_20_30.getTime()));
	}

	@Test
	@DisplayName("과거 날짜로 시간 슬롯 조회 시 400 에러가 반환된다")
	void findAll_pastDate() throws Exception {
		// when & then
		mockMvc.perform(get("/api/time-slots")
				.param(DATE_STR, formatToDateString(LocalDate.now().minusDays(1))))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(TimeSlotException.PAST_DATE_NOT_ALLOWED.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(TimeSlotException.PAST_DATE_NOT_ALLOWED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(TimeSlotException.PAST_DATE_NOT_ALLOWED.getCode()));
	}

	@Test
	@DisplayName("디렉터의 ONGOING 예약이 있는 시간 슬롯은 selectable이 false로 반환된다")
	void findAll_withDirectorMemberId() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.save("소개글", "주소");
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);

		DirectorService parentService = directorCategoryProvider.save("상위 서비스", null);
		DirectorService childService = directorCategoryProvider.save("하위 서비스", parentService);
		ServiceRequest serviceRequest = serviceRequestProvider.savePending(childService, requester);
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveOngoing(directorInfo, serviceRequest,
			LocalDateTime.now());

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(get("/api/time-slots")
				.param(DATE_STR, "2099.12.01")
				.param(DIRECTOR_MEMBER_ID, String.valueOf(director.getId())))
			.andExpect(status().isOk())
			.andReturn();

		// then
		String content = result.getResponse().getContentAsString();
		TimeSlotFindAllResponse response = objectMapper.readValue(content, TimeSlotFindAllResponse.class);

		assertThat(response.getSlots()).hasSize(24);
	}
}
