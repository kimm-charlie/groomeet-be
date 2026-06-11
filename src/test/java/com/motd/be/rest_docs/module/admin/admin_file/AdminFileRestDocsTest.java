package com.motd.be.rest_docs.module.admin.admin_file;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.rest_docs.Utils.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.admin.admin_file.dto.request.AdminFileDeleteRequestForAdmin;
import com.motd.be.module.admin.admin_file.dto.request.AdminFileUpdateProcessRequestForAdmin;
import com.motd.be.module.admin.admin_file.dto.request.AdminFileUploadRequestForAdmin;
import com.motd.be.module.admin.admin_file.dto.response.AdminFileUploadResponseForAdmin;
import com.motd.be.module.member.file.enums.FileProcessStatus;
import com.motd.be.shared.aws.enums.S3DirectoryType;
import com.motd.be.shared.aws.enums.UploadFileType;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class AdminFileRestDocsTest extends BaseRestDocsTest {

	@Test
	void 관리자_Presigned_url_발급_문서화() throws Exception {
		authenticationSetUp();

		AdminFileUploadRequestForAdmin requestBody = AdminFileUploadRequestForAdmin.builder()
			.directoryType(S3DirectoryType.FILE.name())
			.fileType(UploadFileType.IMAGE.name())
			.fileExtension(UploadFileType.IMAGE.getAllowedExtensions()
				.iterator()
				.next())
			.fileName("banner")
			.fileSize("20MB")
			.build();

		willReturn(AdminFileUploadResponseForAdmin.builder()
			.presignedUrl(PRESIGNED_URL)
			.imageId(1L)
			.cdnUrl(CDN_URL_STR)
			.build())
			.given(adminFileFacadeForAdmin)
			.createPresignedUrl(any(Long.class), any(AdminFileUploadRequestForAdmin.class));

		mockMvc.perform(post("/api/admin/files/presigned-url")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestBody)))
			.andExpect(status().isCreated())
			.andDo(document("admin-file-create-presigned-url",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키")
				),

				requestFields(
					fieldWithPath("directoryType").type(JsonFieldType.STRING)
						.attributes(enumFormat(S3DirectoryType.class, Enum::name))
						.description("presigned-url 발급을 원하는 컨텐츠 타입"),

					fieldWithPath("fileType").type(JsonFieldType.STRING)
						.attributes(enumFormat(UploadFileType.class, Enum::name))
						.description("업로드할 파일 유형"),

					fieldWithPath("fileExtension").type(JsonFieldType.STRING)
						.attributes(
							key("format").value(
								UploadFileType.IMAGE.name() + "("
									+ String.join(", ", UploadFileType.IMAGE.getAllowedExtensions())
									+ ")"
							)
						)
						.description("업로드할 파일 확장자"),

					fieldWithPath("fileName").type(JsonFieldType.STRING)
						.optional()
						.description("파일 이름"),

					fieldWithPath("fileSize").type(JsonFieldType.STRING)
						.description("파일 크기")
				),

				responseFields(
					fieldWithPath("presignedUrl").type(JsonFieldType.STRING)
						.description("presigned-url"),

					fieldWithPath("imageId").type(JsonFieldType.NUMBER)
						.description("이미지 Id"),

					fieldWithPath("cdnUrl").type(JsonFieldType.STRING)
						.description("cdn-url")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 이미지 업로드용 Presigned-url 발급 API")
					.description("관리자 이미지 업로드용 Presigned-url 발급 API")
					.build()
				)
			));
	}

	@Test
	void 관리자_이미지_삭제_문서화() throws Exception {
		authenticationSetUp();

		AdminFileDeleteRequestForAdmin requestBody = AdminFileDeleteRequestForAdmin.builder()
			.fileIds(java.util.Arrays.asList(1L, 2L))
			.directoryType(S3DirectoryType.FILE.name())
			.build();

		willDoNothing()
			.given(adminFileFacadeForAdmin).deleteByIds(any(Long.class), any(AdminFileDeleteRequestForAdmin.class));

		mockMvc.perform(delete("/api/admin/files")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestBody)))
			.andExpect(status().isNoContent())
			.andDo(document("admin-file-delete",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키")
				),

				requestFields(
					fieldWithPath("fileIds").type(JsonFieldType.ARRAY).description("이미지 또는 파일 ID 목록"),
					fieldWithPath("directoryType").type(JsonFieldType.STRING)
						.attributes(enumFormat(S3DirectoryType.class, Enum::name))
						.description("이미지 삭제를 원하는 디렉토리 타입")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 이미지 삭제 API")
					.description("관리자 이미지 삭제 API")
					.build()
				)
			));
	}

	@Test
	void 관리자_파일_처리_상태_업데이트_문서화() throws Exception {
		AdminFileUpdateProcessRequestForAdmin requestBody = AdminFileUpdateProcessRequestForAdmin.builder()
			.fileKey("file/test-file-key")
			.processStatus(FileProcessStatus.PROCESSED)
			.build();

		willDoNothing()
			.given(adminFileFacadeForAdmin)
			.updateProcessStatus(anyString(), any(AdminFileUpdateProcessRequestForAdmin.class));

		mockMvc.perform(post("/api/admin/files/processed")
				.header(X_API_KEY, "test-api-key")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestBody)))
			.andExpect(status().isNoContent())
			.andDo(document("admin-file-update-process-status",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestHeaders(
					headerWithName(X_API_KEY)
						.description("API 인증 키 (Lambda 콜백용)")
				),

				requestFields(
					fieldWithPath("fileKey").type(JsonFieldType.STRING)
						.description("S3 파일 키"),
					fieldWithPath("processStatus").type(JsonFieldType.STRING)
						.attributes(enumFormat(FileProcessStatus.class, Enum::name))
						.description("업데이트할 파일 처리 상태")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 파일 처리 상태 업데이트 API (Lambda 콜백)")
					.description("Lambda에서 파일 처리 완료 후 호출하는 관리자 파일 처리 상태 업데이트 API")
					.build()
				)
			));
	}
}
