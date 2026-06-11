package com.motd.be.rest_docs.module.member.time_slot;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.utils.DateFormatUtils.*;
import static com.motd.be.rest_docs.Utils.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.TimeSlotException;
import com.motd.be.module.member.time_slot.dto.response.TimeSlotFindAllResponse;
import com.motd.be.module.member.time_slot.dto.response.TimeSlotResponse;
import com.motd.be.module.member.time_slot.entity.TimeSlot;

@RestDocsTest
public class TimeSlotRestDocsTest extends BaseRestDocsTest {

	@Test
	void 시간_슬롯_전체_조회() throws Exception {

		TimeSlotFindAllResponse response = TimeSlotFindAllResponse.builder()
			.slots(List.of(
				TimeSlotResponse.builder().time(formatToTimeString(TimeSlot.SLOT_09_00.getTime())).selectable(true).build(),
				TimeSlotResponse.builder().time(formatToTimeString(TimeSlot.SLOT_09_30.getTime())).selectable(true).build(),
				TimeSlotResponse.builder().time(formatToTimeString(TimeSlot.SLOT_10_00.getTime())).selectable(false).build(),
				TimeSlotResponse.builder().time(formatToTimeString(TimeSlot.SLOT_10_30.getTime())).selectable(true).build()
			))
			.build();

		given(timeSlotFacade.findAll(any(), any())).willReturn(response);

		mockMvc.perform(get("/api/time-slots")
				.param(DATE_STR, "2024.03.15")
				.param(DIRECTOR_MEMBER_ID, "123")
				.contentType(MediaType.APPLICATION_JSON)
			)
			.andExpect(status().isOk())
			.andDo(document("time-slot-find-all",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("date")
						.attributes(getDateFormat())
						.description("날짜"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("directorMemberId")
						.optional()
						.description("디렉터 회원 ID (다이렉트 요청 시 사용, 없으면 전체 selectable)")
				),

				responseFields(
					fieldWithPath("slots").type(JsonFieldType.ARRAY).description("시간 슬롯 목록"),
					fieldWithPath("slots[].time").type(JsonFieldType.STRING)
						.attributes(getTimeFormat())
						.description("시간"),
					fieldWithPath("slots[].selectable").type(JsonFieldType.BOOLEAN).description("선택 가능 여부")
				),

				resource(builder()
					.tag("⏰ 시간 슬롯 API")
					.summary("시간 슬롯 조회")
					.description("희망시간 슬롯 목록을 조회합니다. directorMemberId가 있으면 해당 디렉터의 ONGOING 예약 시간대를 선택 불가로 표시합니다.")
					.build())
			));
	}
}
