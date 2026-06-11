package com.motd.be.rest_docs.module.member.review;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static com.motd.be.common.utils.DateFormatUtils.*;
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
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.member.director_service.dto.response.DirectorServiceWithFullNameResponse;
import com.motd.be.module.member.file.dto.response.FileResponse;
import com.motd.be.module.member.member.dto.response.MemberResponse;
import com.motd.be.module.member.review.dto.request.ReviewSaveAndUpdateRequest;
import com.motd.be.module.member.review.dto.request.ReviewUpdateRequest;
import com.motd.be.module.member.review.dto.response.ReviewFindAllForDirectorResponse;
import com.motd.be.module.member.review.dto.response.ReviewFindAllForMemberResponse;
import com.motd.be.module.member.review.dto.response.ReviewWithDirectorResponse;
import com.motd.be.module.member.review.dto.response.ReviewWithReceivedCompletedEstimateCountResponse;
import com.motd.be.shared.aws.enums.UploadFileType;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class ReviewRestDocsTest extends BaseRestDocsTest {

	@Test
	void 리뷰_저장() throws Exception {
		authenticationSetUp();

		Long serviceEstimateId = 1L;

		ReviewSaveAndUpdateRequest request = ReviewSaveAndUpdateRequest.builder()
			.content("매우 만족스러운 서비스였습니다!")
			.fileIds(Arrays.asList(1L, 2L))
			.build();

		willDoNothing().given(reviewFacade).saveByMember(anyLong(), anyLong(), any(ReviewSaveAndUpdateRequest.class));

		mockMvc.perform(post("/api/reviews/service-estimates/{serviceEstimateId}", serviceEstimateId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isCreated())
			.andDo(document("review-save",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("serviceEstimateId")
						.description("서비스 제안 ID")
				),

				requestFields(
					fieldWithPath("content").type(JsonFieldType.STRING).description("리뷰 내용"),
					fieldWithPath("fileIds").type(JsonFieldType.ARRAY).description("이미지 또는 파일 ID 목록").optional()
				),

				resource(builder()
					.tag("📝 리뷰 API")
					.summary("리뷰 저장")
					.description("회원이 완료된 서비스 제안에 대해 리뷰를 작성합니다.")
					.build())
			));
	}

	@Test
	void 리뷰_수정() throws Exception {
		authenticationSetUp();

		Long reviewId = 1L;

		ReviewUpdateRequest request = ReviewUpdateRequest.builder()
			.content("리뷰 수정 내용")
			.fileIds(Arrays.asList(3L, 4L))
			.build();

		willDoNothing().given(reviewFacade).updateByMember(anyLong(), anyLong(), any(ReviewSaveAndUpdateRequest.class));

		mockMvc.perform(patch("/api/reviews/{reviewId}", reviewId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isNoContent())
			.andDo(document("review-update",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("reviewId")
						.description("리뷰 ID")
				),

				requestFields(
					fieldWithPath("content").type(JsonFieldType.STRING).description("리뷰 내용"),
					fieldWithPath("fileIds").type(JsonFieldType.ARRAY).description("이미지 또는 파일 ID 목록").optional()
				),

				resource(builder()
					.tag("📝 리뷰 API")
					.summary("리뷰 수정")
					.description("회원이 작성한 리뷰 내용을 수정합니다. 이미지가 포함된 경우 새롭게 매핑합니다.")
					.build())
			));
	}

	@Test
	void 리뷰_삭제() throws Exception {
		authenticationSetUp();

		Long reviewId = 1L;

		willDoNothing().given(reviewFacade).deleteByMember(anyLong(), anyLong());

		mockMvc.perform(delete("/api/reviews/{reviewId}", reviewId)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isNoContent())
			.andDo(document("review-delete",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("reviewId")
						.description("리뷰 ID")
				),

				resource(builder()
					.tag("📝 리뷰 API")
					.summary("리뷰 삭제")
					.description("회원이 작성한 리뷰를 삭제합니다. 해당 리뷰에 연결된 이미지는 모두 비활성화됩니다.")
					.build())
			));
	}

	@Test
	void 특정_제안의_리뷰_조회() throws Exception {
		authenticationSetUp();

		Long serviceEstimateId = 1L;

		ReviewWithReceivedCompletedEstimateCountResponse response = ReviewWithReceivedCompletedEstimateCountResponse.builder()
			.id(1L)
			.writer(MemberResponse.builder()
				.id(1L)
				.nickname(NICKNAME_STR)
				.profileImageUrl(IMAGE_URL_STR)
				.isWithdrawal(false)
				.build())
			.receivedCompletedEstimateCount(5)
			.createdAt(formatToDateString(LocalDateTime.now()))
			.title("제목")
			.content("매우 만족스러운 서비스였습니다!")
			.service(DirectorServiceWithFullNameResponse.builder()
				.id(1L)
				.name("헬스 케어")
				.fullName("건강 > 헬스 케어")
				.build())
			.files(Arrays.asList(
				FileResponse.builder()
					.id(1L)
					.fileUrl(IMAGE_URL_STR)
					.fileType(UploadFileType.IMAGE)
					.fileSize("20MB")
					.fileName(null)
					.build(),
				FileResponse.builder()
					.id(2L)
					.fileUrl(IMAGE_URL_STR)
					.fileType(UploadFileType.IMAGE)
					.fileSize("20MB")
					.fileName(null)
					.build()
			))
			.isEditable(false)
			.build();

		given(reviewFacade.findByServiceEstimate(anyLong(), anyLong())).willReturn(response);

		mockMvc.perform(get("/api/service-estimates/{serviceEstimateId}/reviews", serviceEstimateId)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isOk())
			.andDo(document("review-find-by-service-estimate",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("serviceEstimateId")
						.description("서비스 제안 ID")
				),

				responseFields(
					fieldWithPath("id").type(JsonFieldType.NUMBER).description("리뷰 ID"),
					fieldWithPath("writer").type(JsonFieldType.OBJECT).description("작성자 정보"),
					fieldWithPath("writer.id").type(JsonFieldType.NUMBER).description("작성자 ID"),
					fieldWithPath("writer.nickname").type(JsonFieldType.STRING).description("작성자 닉네임"),
					fieldWithPath("writer.profileImageUrl").type(JsonFieldType.STRING)
						.description("작성자 프로필 이미지 URL"),
					fieldWithPath("writer.isWithdrawal").type(JsonFieldType.BOOLEAN)
						.description("회원탈퇴 여부"),

					fieldWithPath("receivedCompletedEstimateCount").type(JsonFieldType.NUMBER)
						.description("작성자가 받은 완료된 제안 수"),

					fieldWithPath("createdAt").type(JsonFieldType.STRING).description("리뷰 작성일"),
					fieldWithPath("title").type(JsonFieldType.STRING).description("리뷰 제목"),
					fieldWithPath("content").type(JsonFieldType.STRING).description("리뷰 내용"),
					fieldWithPath("service").type(JsonFieldType.OBJECT).description("서비스 정보"),
					fieldWithPath("service.id").type(JsonFieldType.NUMBER).description("서비스 ID"),
					fieldWithPath("service.name").type(JsonFieldType.STRING).description("서비스 이름"),
					fieldWithPath("service.fullName").type(JsonFieldType.STRING)
						.description("서비스 전체 경로 이름"),

					fieldWithPath("files").type(JsonFieldType.ARRAY).optional().description("리뷰 파일 목록"),
					fieldWithPath("files[].id").type(JsonFieldType.NUMBER).optional().description("파일 ID"),
					fieldWithPath("files[].fileUrl").type(JsonFieldType.STRING).optional().description("파일 URL"),
					fieldWithPath("files[].fileType").type(JsonFieldType.STRING)
						.optional()
						.attributes(enumFormat(UploadFileType.class, Enum::name))
						.description("파일 타입"),
					fieldWithPath("files[].fileName").type(JsonFieldType.STRING).optional().description("파일 이름"),
					fieldWithPath("files[].fileSize").type(JsonFieldType.STRING).optional().description("파일 크기"),
					fieldWithPath("isEditable").type(JsonFieldType.BOOLEAN).description("수정 가능 여부")
				),

				resource(builder()
					.tag("📝 리뷰 API")
					.summary("특정 제안의 리뷰 조회")
					.description("특정 서비스 제안에 작성된 리뷰를 조회합니다.")
					.build())
			));
	}

	@Test
	void 내가_작성한_리뷰_전체_조회() throws Exception {
		authenticationSetUp();

		ReviewFindAllForMemberResponse response = ReviewFindAllForMemberResponse.builder()
			.page(0)
			.hasNext(false)
			.reviewCount(2)
			.reviews(Arrays.asList(
				ReviewWithDirectorResponse.builder()
					.id(1L)
					.title("제목")
					.writer(MemberResponse.builder()
						.id(1L)
						.nickname(NICKNAME_STR)
						.profileImageUrl(IMAGE_URL_STR)
						.isWithdrawal(false)
						.build())
					.director(MemberResponse.builder()
						.id(2L)
						.nickname("디렉터1")
						.profileImageUrl(IMAGE_URL_STR)
						.isWithdrawal(false)
						.build())
					.createdAt(formatToDateString(LocalDateTime.now()))
					.content("매우 만족스러운 서비스였습니다!")
					.service(DirectorServiceWithFullNameResponse.builder()
						.id(1L)
						.name("헬스 케어")
						.fullName("건강 > 헬스 케어")
						.build())
					.files(Arrays.asList(
						FileResponse.builder()
							.id(1L)
							.fileUrl(IMAGE_URL_STR)
							.build()
					))
					.isEditable(true)
					.build(),
				ReviewWithDirectorResponse.builder()
					.id(2L)
					.title("제목")
					.writer(MemberResponse.builder()
						.id(1L)
						.nickname(NICKNAME_STR)
						.profileImageUrl(IMAGE_URL_STR)
						.isWithdrawal(false)
						.build())
					.director(MemberResponse.builder()
						.id(3L)
						.nickname("디렉터2")
						.profileImageUrl(IMAGE_URL_STR)
						.isWithdrawal(false)
						.build())
					.createdAt(formatToDateString(LocalDateTime.now().minusDays(2)))
					.content("친절하고 전문적인 서비스였습니다.")
					.service(DirectorServiceWithFullNameResponse.builder()
						.id(2L)
						.name("필라테스")
						.fullName("운동 > 필라테스")
						.build())
					.files(Arrays.asList())
					.isEditable(false)
					.build()
			))
			.build();

		given(reviewFacade.findAllByMember(anyLong(), anyInt())).willReturn(response);

		mockMvc.perform(get("/api/members/my/reviews")
				.param(PAGE_STR, ZERO_STR)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isOk())
			.andDo(document("review-find-my-reviews",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("page")
						.optional()
						.description("페이지 번호 (0부터 시작)")
				),

				responseFields(
					fieldWithPath("page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
					fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
					fieldWithPath("reviewCount").type(JsonFieldType.NUMBER).description("전체 리뷰 수"),
					fieldWithPath("reviews").type(JsonFieldType.ARRAY).description("리뷰 목록"),
					fieldWithPath("reviews[].id").type(JsonFieldType.NUMBER).description("리뷰 ID"),
					fieldWithPath("reviews[].isEditable").type(JsonFieldType.BOOLEAN).description("수정 가능 여부"),
					fieldWithPath("reviews[].title").type(JsonFieldType.STRING).description("리뷰 제목"),
					fieldWithPath("reviews[].writer").type(JsonFieldType.OBJECT).description("작성자 정보"),
					fieldWithPath("reviews[].writer.id").type(JsonFieldType.NUMBER).description("작성자 ID"),
					fieldWithPath("reviews[].writer.nickname").type(JsonFieldType.STRING).description("작성자 닉네임"),
					fieldWithPath("reviews[].writer.profileImageUrl").type(JsonFieldType.STRING)
						.description("작성자 프로필 이미지 URL"),
					fieldWithPath("reviews[].writer.isWithdrawal").type(JsonFieldType.BOOLEAN)
						.description("회원 탈퇴 여부"),

					fieldWithPath("reviews[].director").type(JsonFieldType.OBJECT).description("디렉터 정보"),
					fieldWithPath("reviews[].director.id").type(JsonFieldType.NUMBER).description("디렉터 ID"),
					fieldWithPath("reviews[].director.nickname").type(JsonFieldType.STRING).description("디렉터 닉네임"),
					fieldWithPath("reviews[].director.profileImageUrl").type(JsonFieldType.STRING)
						.description("디렉터 프로필 이미지 URL"),
					fieldWithPath("reviews[].director.isWithdrawal").type(JsonFieldType.BOOLEAN)
						.description("디렉터 탈퇴 여부"),

					fieldWithPath("reviews[].createdAt").type(JsonFieldType.STRING).description("리뷰 작성일"),
					fieldWithPath("reviews[].content").type(JsonFieldType.STRING).description("리뷰 내용"),
					fieldWithPath("reviews[].service").type(JsonFieldType.OBJECT).description("서비스 정보"),
					fieldWithPath("reviews[].service.id").type(JsonFieldType.NUMBER).description("서비스 ID"),
					fieldWithPath("reviews[].service.name").type(JsonFieldType.STRING).description("서비스 이름"),
					fieldWithPath("reviews[].service.fullName").type(JsonFieldType.STRING)
						.description("서비스 전체 경로 이름"),
					fieldWithPath("reviews[].files").type(JsonFieldType.ARRAY).optional().description("리뷰 파일 목록"),
					fieldWithPath("reviews[].files[].id").type(JsonFieldType.NUMBER).optional().description("파일 ID"),
					fieldWithPath("reviews[].files[].fileUrl").type(JsonFieldType.STRING)
						.optional()
						.description("파일 URL"),
					fieldWithPath("reviews[].files[].fileType").type(JsonFieldType.STRING).optional()
						.attributes(enumFormat(UploadFileType.class, Enum::name))
						.description("파일 타입"),
					fieldWithPath("reviews[].files[].fileName").type(JsonFieldType.STRING)
						.optional()
						.description("파일 이름"),
					fieldWithPath("reviews[].files[].fileSize").type(JsonFieldType.STRING)
						.optional()
						.description("파일 크기")
				),

				resource(builder()
					.tag("📝 리뷰 API")
					.summary("내가 작성한 리뷰 전체 조회")
					.description("로그인한 회원이 작성한 모든 리뷰 목록을 조회합니다.")
					.build())
			));
	}

	@Test
	void 디렉터가_받은_리뷰_전체_조회() throws Exception {
		authenticationSetUp();
		Long targetMemberId = 1L;

		ReviewFindAllForDirectorResponse response = ReviewFindAllForDirectorResponse.builder()
			.page(0)
			.hasNext(false)
			.reviewCount(2)
			.reviews(Arrays.asList(
				ReviewWithReceivedCompletedEstimateCountResponse.builder()
					.id(1L)
					.writer(MemberResponse.builder()
						.id(1L)
						.nickname(NICKNAME_STR)
						.profileImageUrl(IMAGE_URL_STR)
						.isWithdrawal(false)
						.build())
					.receivedCompletedEstimateCount(5)
					.createdAt(formatToDateString(LocalDateTime.now()))
					.title("제목")
					.content("매우 만족스러운 서비스였습니다!")
					.service(DirectorServiceWithFullNameResponse.builder()
						.id(1L)
						.name("헬스 케어")
						.fullName("건강 > 헬스 케어")
						.build())
					.files(Arrays.asList(
						FileResponse.builder()
							.id(1L)
							.fileUrl(IMAGE_URL_STR)
							.build(),
						FileResponse.builder()
							.id(2L)
							.fileUrl(IMAGE_URL_STR)
							.build()
					))
					.isEditable(false)
					.build(),
				ReviewWithReceivedCompletedEstimateCountResponse.builder()
					.id(2L)
					.writer(MemberResponse.builder()
						.id(2L)
						.nickname("테스트유저2")
						.profileImageUrl(IMAGE_URL_STR)
						.isWithdrawal(false)
						.build())
					.receivedCompletedEstimateCount(3)
					.createdAt(formatToDateString(LocalDateTime.now().minusDays(1)))
					.title("제목")
					.content("전문적이고 친절한 서비스였습니다.")
					.service(DirectorServiceWithFullNameResponse.builder()
						.id(2L)
						.name("필라테스")
						.fullName("운동 > 필라테스")
						.build())
					.files(Arrays.asList())
					.isEditable(false)
					.build()
			))
			.build();

		given(reviewFacade.findAllByDirectorAndService(anyLong(), anyInt(), anyLong(), anyLong())).willReturn(response);

		mockMvc.perform(get("/api/directors/{targetMemberId}/reviews", targetMemberId)
				.param(PAGE_STR, ZERO_STR)
				.param(DIRECTOR_SERVICE_ID_STR, "1")
			)
			.andExpect(status().isOk())
			.andDo(document("review-find-all-for-director",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("targetMemberId")
						.description("디렉터 회원 ID")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("page")
						.optional()
						.description("페이지 번호 (0부터 시작)"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("directorServiceId")
						.optional()
						.description("디렉터 서비스 ID (필터링용)")
				),

				responseFields(
					fieldWithPath("page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
					fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
					fieldWithPath("reviewCount").type(JsonFieldType.NUMBER).description("전체 리뷰 수"),
					fieldWithPath("reviews").type(JsonFieldType.ARRAY).description("리뷰 목록"),
					fieldWithPath("reviews[].id").type(JsonFieldType.NUMBER).description("리뷰 ID"),
					fieldWithPath("reviews[].isEditable").type(JsonFieldType.BOOLEAN).description("수정 가능 여부"),
					fieldWithPath("reviews[].writer").type(JsonFieldType.OBJECT).description("작성자 정보"),
					fieldWithPath("reviews[].writer.id").type(JsonFieldType.NUMBER).description("작성자 ID"),
					fieldWithPath("reviews[].writer.nickname").type(JsonFieldType.STRING).description("작성자 닉네임"),
					fieldWithPath("reviews[].writer.profileImageUrl").type(JsonFieldType.STRING)
						.description("작성자 프로필 이미지 URL"),
					fieldWithPath("reviews[].writer.isWithdrawal").type(JsonFieldType.BOOLEAN)
						.description("회원탈퇴 여부"),
					fieldWithPath("reviews[].receivedCompletedEstimateCount").type(JsonFieldType.NUMBER)
						.description("작성자가 받은 완료된 제안 수"),
					fieldWithPath("reviews[].createdAt").type(JsonFieldType.STRING).description("리뷰 작성일"),
					fieldWithPath("reviews[].title").type(JsonFieldType.STRING).description("리뷰 제목"),
					fieldWithPath("reviews[].content").type(JsonFieldType.STRING).description("리뷰 내용"),
					fieldWithPath("reviews[].service").type(JsonFieldType.OBJECT).description("서비스 정보"),
					fieldWithPath("reviews[].service.id").type(JsonFieldType.NUMBER).description("서비스 ID"),
					fieldWithPath("reviews[].service.name").type(JsonFieldType.STRING).description("서비스 이름"),
					fieldWithPath("reviews[].service.fullName").type(JsonFieldType.STRING)
						.description("서비스 전체 경로 이름"),

					fieldWithPath("reviews[].files").type(JsonFieldType.ARRAY).optional().description("리뷰 파일 목록"),
					fieldWithPath("reviews[].files[].id").type(JsonFieldType.NUMBER).optional().description("파일 ID"),
					fieldWithPath("reviews[].files[].fileUrl").type(JsonFieldType.STRING)
						.optional()
						.description("파일 URL"),
					fieldWithPath("reviews[].files[].fileType").type(JsonFieldType.STRING)
						.optional()
						.attributes(enumFormat(UploadFileType.class, Enum::name))
						.description("파일 타입"),
					fieldWithPath("reviews[].files[].fileName").type(JsonFieldType.STRING)
						.optional()
						.description("파일 이름"),
					fieldWithPath("reviews[].files[].fileSize").type(JsonFieldType.STRING)
						.optional()
						.description("파일 크기")
				),

				resource(builder()
					.tag("📝 리뷰 API")
					.summary("디렉터가 받은 리뷰 전체 조회")
					.description("특정 디렉터가 받은 모든 리뷰 목록을 조회합니다. 디렉터 서비스 ID로 필터링 가능합니다.")
					.build())
			));
	}
}
