package com.motd.be.rest_docs.module.director.service_estimate_template;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static com.motd.be.rest_docs.Utils.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.director.file.dto.response.FileResponseForDirector;
import com.motd.be.module.director.service_estimate_template.dto.request.ServiceEstimateTemplateSaveAndUpdateRequestForDirector;
import com.motd.be.module.director.service_estimate_template.dto.response.ServiceEstimateTemplateFindAllResponseForDirector;
import com.motd.be.module.director.service_estimate_template.dto.response.ServiceEstimateTemplateFindDetailResponseForDirector;
import com.motd.be.module.member.director_service.dto.response.DirectorServiceResponse;
import com.motd.be.shared.aws.enums.UploadFileType;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class ServiceEstimateTemplateForDirectorRestDocsTest extends BaseRestDocsTest {

	@Test
	void 서비스_제안_템플릿_전체_조회() throws Exception {
		authenticationSetUp();

		List<ServiceEstimateTemplateFindAllResponseForDirector> responses = Arrays.asList(
			ServiceEstimateTemplateFindAllResponseForDirector.builder()
				.id(1L)
				.serviceName("헤어 커트")
				.title("남성 커트 베이직")
				.price(20000L)
				.build(),
			ServiceEstimateTemplateFindAllResponseForDirector.builder()
				.id(2L)
				.serviceName("염색")
				.title("부분 염색")
				.price(50000L)
				.build()
		);

		given(serviceEstimateTemplateFacadeForDirector.findAll(anyLong(), anyLong())).willReturn(responses);

		// when & then
		mockMvc.perform(get("/api/directors/my/services/estimate-templates")
				.param("serviceId", "1")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isOk())
			.andDo(document("service-estimate-template-find-all",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("serviceId")
						.optional()
						.description("서비스 ID")
				),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				responseFields(
					fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("템플릿 ID"),
					fieldWithPath("[].serviceName").type(JsonFieldType.STRING).description("서비스명"),
					fieldWithPath("[].title").type(JsonFieldType.STRING).description("템플릿 제목"),
					fieldWithPath("[].price").type(JsonFieldType.NUMBER).description("가격")
				),

				resource(builder()
					.tag("🧾 서비스 제안 템플릿 API")
					.summary("서비스 제안 템플릿 전체 조회")
					.description("디렉터가 자신의 서비스 제안 템플릿 목록을 조회합니다.")
					.build())
			));
	}

	@Test
	void 서비스_제안_템플릿_상세_조회_템플릿_아이디_기반() throws Exception {
		// given
		authenticationSetUp();

		Long templateId = 100L;

		ServiceEstimateTemplateFindDetailResponseForDirector response = ServiceEstimateTemplateFindDetailResponseForDirector.builder()
			.id(templateId)
			.service(DirectorServiceResponse.builder()
				.id(1L)
				.name(SERVICE_NAME_1_STR)
				.build())
			.title("남성 커트 베이직")
			.price(20000L)
			.content("기본 커트에 샴푸 포함")
			.files(Arrays.asList(
				FileResponseForDirector.builder()
					.id(1L)
					.fileUrl(IMAGE_URL_STR)
					.fileType(UploadFileType.IMAGE)
					.fileSize("20MB")
					.fileName(null)
					.build(),
				FileResponseForDirector.builder()
					.id(2L)
					.fileUrl(IMAGE_URL_STR)
					.fileType(UploadFileType.IMAGE)
					.fileSize("20MB")
					.fileName(null)
					.build()
			))
			.build();

		given(serviceEstimateTemplateFacadeForDirector.findDetailByTemplateId(anyLong(), anyLong())).willReturn(
			response);

		// when & then
		mockMvc.perform(
				get("/api/directors/my/services/estimate-templates/{templateId}", templateId)
					.cookie(
						new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
						new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
					)
			)
			.andExpect(status().isOk())
			.andDo(document("service-estimate-template-find-detail-by-template-id",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("templateId")
						.description("템플릿 ID")
				),

				responseFields(
					fieldWithPath("id").type(JsonFieldType.NUMBER).description("템플릿 ID"),
					fieldWithPath("title").type(JsonFieldType.STRING).description("템플릿 제목"),
					fieldWithPath("price").type(JsonFieldType.NUMBER).description("가격"),
					fieldWithPath("content").type(JsonFieldType.STRING).description("내용"),

					fieldWithPath("service").type(JsonFieldType.OBJECT).description("서비스 정보"),
					fieldWithPath("service.id").type(JsonFieldType.NUMBER).description("서비스 ID"),
					fieldWithPath("service.name").type(JsonFieldType.STRING).description("서비스명"),

					fieldWithPath("files").type(JsonFieldType.ARRAY).optional().description("리뷰 파일 목록"),
					fieldWithPath("files[].id").type(JsonFieldType.NUMBER).optional().description("파일 ID"),
					fieldWithPath("files[].fileUrl").type(JsonFieldType.STRING).optional().description("파일 URL"),
					fieldWithPath("files[].fileType").type(JsonFieldType.STRING)
						.optional()
						.attributes(enumFormat(UploadFileType.class, Enum::name))
						.description("파일 타입"),
					fieldWithPath("files[].fileName").type(JsonFieldType.STRING).optional().description("파일 이름"),
					fieldWithPath("files[].fileSize").type(JsonFieldType.STRING).optional().description("파일 크기")
				),

				resource(builder()
					.tag("🧾 서비스 제안 템플릿 API")
					.summary("서비스 제안 템플릿 상세 조회 (템플릿 아이디 기반)")
					.description("디렉터가 특정 서비스의 제안 템플릿 상세 정보를 조회합니다.")
					.build())
			));
	}

	@Test
	void 서비스_제안_템플릿_생성() throws Exception {
		authenticationSetUp();

		ServiceEstimateTemplateSaveAndUpdateRequestForDirector request = ServiceEstimateTemplateSaveAndUpdateRequestForDirector.builder()
			.serviceId(10L)
			.price(30000L)
			.title("남성 커트 프리미엄")
			.content("샴푸+드라이 포함")
			.fileIds(Arrays.asList(1L, 2L))
			.build();

		willDoNothing().given(serviceEstimateTemplateFacadeForDirector)
			.save(anyLong(), any(ServiceEstimateTemplateSaveAndUpdateRequestForDirector.class));

		mockMvc.perform(post("/api/directors/my/services/estimate-templates")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isCreated())
			.andDo(document("service-estimate-template-save",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				requestFields(
					fieldWithPath("serviceId").type(JsonFieldType.NUMBER).description("서비스 ID"),
					fieldWithPath("price").type(JsonFieldType.NUMBER).description("가격"),
					fieldWithPath("title").type(JsonFieldType.STRING).description("템플릿 제목"),
					fieldWithPath("content").type(JsonFieldType.STRING).description("내용"),
					fieldWithPath("fileIds").type(JsonFieldType.ARRAY).description("이미지 또는 파일 ID 목록").optional()
				),

				resource(builder()
					.tag("🧾 서비스 제안 템플릿 API")
					.summary("서비스 제안 템플릿 생성")
					.description("디렉터가 서비스에 대한 제안 템플릿을 생성합니다.")
					.build())
			));
	}

	@Test
	void 서비스_제안_템플릿_수정() throws Exception {
		// given
		authenticationSetUp();

		Long serviceId = 10L;
		Long templateId = 100L;

		ServiceEstimateTemplateSaveAndUpdateRequestForDirector request = ServiceEstimateTemplateSaveAndUpdateRequestForDirector.builder()
			.serviceId(serviceId)
			.price(35000L)
			.title("남성 커트 프리미엄 - 수정")
			.content("샴푸+드라이 포함 (수정)")
			.fileIds(Arrays.asList(1L, 2L))
			.build();

		willDoNothing().given(serviceEstimateTemplateFacadeForDirector)
			.update(anyLong(), any(ServiceEstimateTemplateSaveAndUpdateRequestForDirector.class), anyLong());

		// when & then
		mockMvc.perform(
				put("/api/directors/my/services/estimate-templates/{templateId}", templateId)
					.cookie(
						new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
						new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
					)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isNoContent())
			.andDo(document("service-estimate-template-update",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("templateId")
						.description("템플릿 ID")
				),

				requestFields(
					fieldWithPath("serviceId").type(JsonFieldType.NUMBER).description("서비스 ID"),
					fieldWithPath("price").type(JsonFieldType.NUMBER).description("가격"),
					fieldWithPath("title").type(JsonFieldType.STRING).description("템플릿 제목"),
					fieldWithPath("content").type(JsonFieldType.STRING).description("내용"),
					fieldWithPath("fileIds").type(JsonFieldType.ARRAY).description("이미지 또는 파일 ID 목록").optional()
				),

				resource(builder()
					.tag("🧾 서비스 제안 템플릿 API")
					.summary("서비스 제안 템플릿 수정")
					.description("디렉터가 특정 서비스의 제안 템플릿을 수정합니다.")
					.build())
			));
	}

	@Test
	void 서비스_제안_템플릿_삭제() throws Exception {
		// given
		authenticationSetUp();

		Long templateId = 100L;

		willDoNothing().given(serviceEstimateTemplateFacadeForDirector)
			.delete(anyLong(), anyLong());

		// when & then
		mockMvc.perform(
				delete("/api/directors/my/services/estimate-templates/{templateId}", templateId)
					.cookie(
						new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
						new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
					)
			)
			.andExpect(status().isNoContent())
			.andDo(document("service-estimate-template-delete",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("templateId")
						.description("템플릿 ID")
				),

				resource(builder()
					.tag("🧾 서비스 제안 템플릿 API")
					.summary("서비스 제안 템플릿 삭제")
					.description("디렉터가 특정 서비스의 제안 템플릿을 삭제합니다.")
					.build())
			));
	}

}
