package com.motd.be.rest_docs.module.member.popular_portfolio;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.member.member.dto.response.MemberResponse;
import com.motd.be.module.member.portfolio.dto.response.PopularPortfolioFindRandomResponse;
import com.motd.be.module.member.portfolio.dto.response.PopularPortfolioResponse;

@RestDocsTest
public class PopularPortfolioControllerRestDocsTest extends BaseRestDocsTest {

	@Test
	void 인기_포트폴리오_랜덤_조회() throws Exception {
		PopularPortfolioFindRandomResponse response = PopularPortfolioFindRandomResponse.builder()
			.portfolios(List.of(
				PopularPortfolioResponse.builder()
					.portfolioId(1L)
					.title("웨딩 스냅 촬영")
					.thumbnailUrl("https://cdn.example.com/thumbnail1.jpg")
					.member(MemberResponse.builder().id(1L).nickname("김작가").profileImageUrl("https://cdn.example.com/profile1.jpg").isWithdrawal(false).build())
					.serviceName("웨딩 스냅")
					.price(150000L)
					.build(),
				PopularPortfolioResponse.builder()
					.portfolioId(2L)
					.title("프로필 촬영")
					.thumbnailUrl("https://cdn.example.com/thumbnail2.jpg")
					.member(MemberResponse.builder().id(2L).nickname("이포토").profileImageUrl("https://cdn.example.com/profile2.jpg").isWithdrawal(false).build())
					.serviceName("프로필 촬영")
					.price(80000L)
					.build(),
				PopularPortfolioResponse.builder()
					.portfolioId(3L)
					.title("가족 사진 촬영")
					.thumbnailUrl("https://cdn.example.com/thumbnail3.jpg")
					.member(MemberResponse.builder().id(3L).nickname("박스튜디오").profileImageUrl("https://cdn.example.com/profile3.jpg").isWithdrawal(false).build())
					.serviceName("가족 사진")
					.price(200000L)
					.build(),
				PopularPortfolioResponse.builder()
					.portfolioId(4L)
					.title("베이비 촬영")
					.thumbnailUrl("https://cdn.example.com/thumbnail4.jpg")
					.member(MemberResponse.builder().id(4L).nickname("최베이비").profileImageUrl("https://cdn.example.com/profile4.jpg").isWithdrawal(false).build())
					.serviceName("베이비 촬영")
					.price(120000L)
					.build()
			))
			.build();

		given(portfolioFacade.findRandom(any())).willReturn(response);

		mockMvc.perform(get("/api/portfolios/popular")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("popular-portfolio-find-random",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				responseFields(
					fieldWithPath("portfolios").type(JsonFieldType.ARRAY).description("인기 포트폴리오 목록 (최대 4개)"),
					fieldWithPath("portfolios[].portfolioId").type(JsonFieldType.NUMBER).description("포트폴리오 ID"),
					fieldWithPath("portfolios[].title").type(JsonFieldType.STRING).description("포트폴리오 제목"),
					fieldWithPath("portfolios[].thumbnailUrl").type(JsonFieldType.STRING).description("썸네일 이미지 URL")
						.optional(),
					fieldWithPath("portfolios[].member").type(JsonFieldType.OBJECT).description("디렉터 회원 정보"),
					fieldWithPath("portfolios[].member.id").type(JsonFieldType.NUMBER).description("회원 ID"),
					fieldWithPath("portfolios[].member.nickname").type(JsonFieldType.STRING).description("닉네임"),
					fieldWithPath("portfolios[].member.profileImageUrl").type(JsonFieldType.STRING).description("프로필 이미지 URL").optional(),
					fieldWithPath("portfolios[].member.isWithdrawal").type(JsonFieldType.BOOLEAN).description("탈퇴 여부"),
					fieldWithPath("portfolios[].serviceName").type(JsonFieldType.STRING).description("서비스명"),
					fieldWithPath("portfolios[].price").type(JsonFieldType.NUMBER).description("가격")
				),

				resource(builder()
					.tag("포트폴리오 API")
					.summary("인기 포트폴리오 랜덤 조회")
					.description("인기 포트폴리오 목록에서 랜덤 4개를 조회합니다. 비로그인 시 차단 필터링 없이 조회됩니다.")
					.build())
			));
	}
}
