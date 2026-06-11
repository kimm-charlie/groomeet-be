package com.motd.be.rest_docs.module.member.consulting_sheet;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.member.consulting_sheet.dto.response.ConsultingSheetDetailResponse;
import com.motd.be.module.member.file.dto.response.FileResponse;
import com.motd.be.module.member.member.dto.response.MemberResponse;
import com.motd.be.shared.aws.enums.UploadFileType;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class ConsultingSheetRestDocsTest extends BaseRestDocsTest {

	@Test
	void 컨설팅지_상세_조회() throws Exception {
		authenticationSetUp();

		// given
		ConsultingSheetDetailResponse response = ConsultingSheetDetailResponse.builder()
			.id(10L)
			.content("앞머리는 시스루뱅으로 가볍게 내리고, 옆머리는 레이어드 컷으로 볼륨감을 살려주세요.")
			.price("50,000원")
			.files(List.of(
				FileResponse.builder()
					.id(301L)
					.fileUrl("https://cdn.example.com/sheet-file-1.jpg")
					.fileType(UploadFileType.IMAGE)
					.fileName("sheet-file-1.jpg")
					.fileSize("1024")
					.build()
			))
			.director(MemberResponse.builder()
				.id(1L)
				.nickname("디렉터A")
				.profileImageUrl("https://cdn.example.com/profile.jpg")
				.isWithdrawal(false)
				.build())
			.createdAt("2026.02.25 14:00")
			.build();

		given(consultingSheetFacade.findApprovedSheetDetail(anyLong(), anyLong())).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/members/consulting-sheets/{consultingSheetId}", 10L)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				))
			.andExpect(status().isOk())
			.andDo(document("consulting-sheet-detail",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("consultingSheetId")
						.description("컨설팅지 ID")
				),

				responseFields(
					fieldWithPath("id").type(JsonFieldType.NUMBER)
						.description("컨설팅지 ID"),
					fieldWithPath("content").type(JsonFieldType.STRING)
						.description("컨설팅지 내용"),
					fieldWithPath("price").type(JsonFieldType.STRING)
						.description("가격 정보"),
					fieldWithPath("files").type(JsonFieldType.ARRAY)
						.description("컨설팅지 첨부 파일 목록"),
					fieldWithPath("files[].id").type(JsonFieldType.NUMBER)
						.description("파일 ID"),
					fieldWithPath("files[].fileUrl").type(JsonFieldType.STRING)
						.description("파일 CDN URL"),
					fieldWithPath("files[].fileType").type(JsonFieldType.STRING)
						.description("파일 타입 (IMAGE, VIDEO)"),
					fieldWithPath("files[].fileName").type(JsonFieldType.STRING)
						.description("파일명"),
					fieldWithPath("files[].fileSize").type(JsonFieldType.STRING)
						.description("파일 크기"),
					fieldWithPath("director").type(JsonFieldType.OBJECT)
						.description("디렉터 회원 정보"),
					fieldWithPath("director.id").type(JsonFieldType.NUMBER)
						.description("디렉터 회원 ID"),
					fieldWithPath("director.nickname").type(JsonFieldType.STRING)
						.description("디렉터 닉네임"),
					fieldWithPath("director.profileImageUrl").type(JsonFieldType.STRING).optional()
						.description("디렉터 프로필 이미지 URL"),
					fieldWithPath("director.isWithdrawal").type(JsonFieldType.BOOLEAN)
						.description("디렉터 탈퇴 여부"),
					fieldWithPath("createdAt").type(JsonFieldType.STRING)
						.description("컨설팅지 생성일시")
				),

				resource(builder()
					.tag("💆 컨설팅 API")
					.summary("컨설팅지 상세 조회")
					.description("승인된 컨설팅지의 상세 내용을 조회합니다. 컨설팅지 내용, 디렉터 정보, 참고 스타일 이미지를 포함합니다.")
					.build())
			));
	}
}
