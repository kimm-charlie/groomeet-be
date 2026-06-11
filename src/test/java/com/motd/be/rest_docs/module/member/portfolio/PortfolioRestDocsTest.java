package com.motd.be.rest_docs.module.member.portfolio;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static com.motd.be.Constants.TARGET_MEMBER_ID;
import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.rest_docs.Utils.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.member.director_service.dto.response.DirectorServiceWithFullNameResponse;
import com.motd.be.module.member.location.dto.response.LocationResponse;
import com.motd.be.module.member.member.dto.response.MemberResponse;
import com.motd.be.module.member.member.dto.response.MemberResponseWithLocation;
import com.motd.be.module.member.portfolio.dto.response.PortfolioFindAllResponse;
import com.motd.be.module.member.portfolio.dto.response.PortfolioFindDetailResponse;
import com.motd.be.module.member.portfolio.dto.response.PortfolioImageResponse;
import com.motd.be.module.member.portfolio.dto.response.PortfolioResponse;
import com.motd.be.module.member.portfolio.entity.PortfolioSortType;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class PortfolioRestDocsTest extends BaseRestDocsTest {

	@Test
	void 포트폴리오_전체_조회() throws Exception {
		authenticationSetUp();

		PortfolioFindAllResponse response = PortfolioFindAllResponse.builder()
			.hasNext(false)
			.portfolios(List.of(
				PortfolioResponse.builder()
					.id(1L)
					.title(TITLE_STR)
					.createdAt(java.time.LocalDateTime.now())
					.thumbnailImageUrl(IMAGE_URL_STR)
					.member(MemberResponse.builder()
						.id(1L)
						.nickname(NICKNAME_STR)
						.profileImageUrl(IMAGE_URL_STR)
						.isWithdrawal(false)
						.build())
					.service(DirectorServiceWithFullNameResponse.builder()
						.id(10L)
						.name("체형 및 비만")
						.fullName("메디컬 > 체형 및 비만")
						.build())
					.build()
			))
			.build();

		willReturn(response).given(portfolioFacade)
			.findAll(anyLong(), any(), anyLong(), anyLong(), anyLong(), anyString(), anyLong());

		mockMvc.perform(get("/api/portfolios")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.param(CURSOR_ID, "1")
				.param(LOCATION_ID_STR, "1")
				.param(DIRECTOR_SERVICE_ID_STR, "1")
				.param(TARGET_MEMBER_ID, "1")
				.param(SORT_TYPE, PortfolioSortType.MOST_HIRED.name())
				.param(EXCLUDE_PORTFOLIO_ID, "1")
			)
			.andExpect(status().isOk())
			.andDo(document("portfolio-find-all",
				getRequestPreProcessor(),
				getResponsePreProcessor(),
				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).optional().description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).optional().description("HttpOnly refreshToken 쿠키")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(CURSOR_ID)
						.optional().description("마지막으로 조회한 포트폴리오 ID (커서)"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(LOCATION_ID_STR)
						.optional().description("지역 아이디"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(DIRECTOR_SERVICE_ID_STR)
						.optional().description("디렉터 서비스 아이디"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(TARGET_MEMBER_ID)
						.optional().description("조회할 대상 멤버 ID (특정 디렉터의 마이페이지에서 조회할때 사용)"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(SORT_TYPE)
						.attributes(enumFormat(PortfolioSortType.class, Enum::name))
						.optional().description("정렬 타입"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(EXCLUDE_PORTFOLIO_ID)
						.optional().description("제외할 포트폴리오 ID")
				),

				responseFields(
					fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
					fieldWithPath("portfolios").type(JsonFieldType.ARRAY).description("포트폴리오 목록"),
					fieldWithPath("portfolios[].id").type(JsonFieldType.NUMBER).description("포트폴리오 ID"),
					fieldWithPath("portfolios[].title").type(JsonFieldType.STRING).description("포트폴리오 제목"),
					fieldWithPath("portfolios[].createdAt").type(JsonFieldType.STRING).description("생성일시"),
					fieldWithPath("portfolios[].thumbnailImageUrl").type(JsonFieldType.STRING)
						.description("포트폴리오 썸네일 이미지"),

					fieldWithPath("portfolios[].member").type(JsonFieldType.OBJECT)
						.description("포트폴리오 작성 디렉터 정보"),
					fieldWithPath("portfolios[].member.id").type(JsonFieldType.NUMBER).description("회원 아이디"),
					fieldWithPath("portfolios[].member.nickname").type(JsonFieldType.STRING).description("닉네임"),
					fieldWithPath("portfolios[].member.profileImageUrl").type(JsonFieldType.STRING)
						.description("프로필 이미지 URL"),
					fieldWithPath("portfolios[].member.isWithdrawal").type(JsonFieldType.BOOLEAN)
						.description("회원 탈퇴 여부"),

					// 서비스 정보
					fieldWithPath("portfolios[].service").type(JsonFieldType.OBJECT).description("디렉터 서비스 정보"),
					fieldWithPath("portfolios[].service.id").type(JsonFieldType.NUMBER).description("서비스 ID"),
					fieldWithPath("portfolios[].service.name").type(JsonFieldType.STRING).description("서비스명"),
					fieldWithPath("portfolios[].service.fullName").type(JsonFieldType.STRING)
						.description("상위 카테고리 포함 전체 경로")
				),
				resource(builder()
					.tag("📁 포트폴리오 API")
					.summary("포트폴리오 전체 조회")
					.description("포트폴리오 전체 목록을 조회합니다.")
					.build())
			));
	}

	@Test
	void 포트폴리오_상세_조회() throws Exception {
		authenticationSetUp();

		Long portfolioId = 1L;
		PortfolioFindDetailResponse response = PortfolioFindDetailResponse.builder()
			.id(portfolioId)
			.title("포트폴리오 제목")
			.content("포트폴리오 내용")
			.createdAt(LocalDateTime.now())
			.price(45000L)
			.service(DirectorServiceWithFullNameResponse.builder()
				.id(10L)
				.name("체형 및 비만")
				.fullName("메디컬 > 체형 및 비만")
				.build())
			.member(MemberResponseWithLocation.builder()
				.id(5L)
				.nickname("motd_director")
				.profileImageUrl("https://cdn.example.com/profile.jpg")
				.locations(List.of(
					LocationResponse.builder()
						.id(101L)
						.name("강남구")
						.fullName("서울 강남구")
						.build()
				))
				.build())
			.files(List.of(
				PortfolioImageResponse.builder()
					.id(10L)
					.fileUrl("https://cdn.example.com/image1.jpg")
					.isThumbnailImage(true)
					.build(),
				PortfolioImageResponse.builder()
					.id(11L)
					.fileUrl("https://cdn.example.com/image2.jpg")
					.isThumbnailImage(false)
					.build()
			))
			.isOwner(true)
			.hasNotEndedRequest(true)
			.isPortfolioFromActiveService(false)
			.build();

		willReturn(response).given(portfolioFacade).findDetail(anyLong(), anyLong());

		mockMvc.perform(get("/api/portfolios/{portfolioId}", portfolioId)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isOk())
			.andDo(document("portfolio-find-detail",
				getRequestPreProcessor(),
				getResponsePreProcessor(),
				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).optional().description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).optional().description("HttpOnly refreshToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("portfolioId")
						.description("조회할 포트폴리오 ID")
				),

				// 응답 필드
				responseFields(
					fieldWithPath("id").type(JsonFieldType.NUMBER).description("포트폴리오 ID"),
					fieldWithPath("title").type(JsonFieldType.STRING).description("포트폴리오 제목"),
					fieldWithPath("content").type(JsonFieldType.STRING).description("포트폴리오 내용"),
					fieldWithPath("price").type(JsonFieldType.NUMBER).description("가격 (원 단위)"),
					fieldWithPath("createdAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("생성일시"),
					fieldWithPath("isOwner").type(JsonFieldType.BOOLEAN).description("본인 소유 여부"),
					fieldWithPath("hasNotEndedRequest").type(JsonFieldType.BOOLEAN)
						.description("끝나지 않은 요청 존재 여부 (true : 존재함, false : 없음)"),
					fieldWithPath("isPortfolioFromActiveService").type(JsonFieldType.BOOLEAN)
						.description("포트폴리오가 활성화된 서비스에서 작성된 것인지 여부"),

					// 이미지 정보
					subsectionWithPath("files").type(JsonFieldType.ARRAY).description("포트폴리오 이미지 목록"),
					fieldWithPath("files[].id").type(JsonFieldType.NUMBER).description("이미지 ID"),
					fieldWithPath("files[].fileUrl").type(JsonFieldType.STRING).description("이미지 CDN URL"),
					fieldWithPath("files[].isThumbnailImage").type(JsonFieldType.BOOLEAN).description("썸네일 이미지 여부"),

					// 서비스 정보
					fieldWithPath("service").type(JsonFieldType.OBJECT).description("디렉터 서비스 정보"),
					fieldWithPath("service.id").type(JsonFieldType.NUMBER).description("서비스 ID"),
					fieldWithPath("service.name").type(JsonFieldType.STRING).description("서비스명"),
					fieldWithPath("service.fullName").type(JsonFieldType.STRING).description("상위 카테고리 포함 전체 경로"),

					// 디렉터(회원) 정보
					fieldWithPath("member").type(JsonFieldType.OBJECT).description("디렉터(회원) 정보"),
					fieldWithPath("member.id").type(JsonFieldType.NUMBER).description("회원 ID"),
					fieldWithPath("member.nickname").type(JsonFieldType.STRING).description("닉네임"),
					fieldWithPath("member.profileImageUrl").type(JsonFieldType.STRING).description("프로필 이미지 URL"),
					fieldWithPath("member.locations").type(JsonFieldType.ARRAY).description("디렉터 활동 지역 목록"),
					fieldWithPath("member.locations[].id").type(JsonFieldType.NUMBER).description("지역 ID"),
					fieldWithPath("member.locations[].name").type(JsonFieldType.STRING).description("지역명"),
					fieldWithPath("member.locations[].fullName").type(JsonFieldType.STRING).description("상위 포함 전체 지역명")
				),

				resource(builder()
					.tag("📁 포트폴리오 API")
					.summary("포트폴리오 상세 조회")
					.description("특정 포트폴리오의 상세 정보를 조회합니다.")
					.build())
			));
	}
}
