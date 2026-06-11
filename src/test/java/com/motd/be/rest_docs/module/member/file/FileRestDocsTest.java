package com.motd.be.rest_docs.module.member.file;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static com.motd.be.Constants.ID;
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

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.member.file.dto.request.FileDeleteRequest;
import com.motd.be.module.member.file.dto.request.FileProcessStatusRequest;
import com.motd.be.module.member.file.dto.request.FileUpdateProcessRequest;
import com.motd.be.module.member.file.dto.request.FileUploadRequest;
import com.motd.be.module.member.file.dto.response.FileProcessStatusResponse;
import com.motd.be.module.member.file.dto.response.FileUploadResponse;
import com.motd.be.module.member.file.enums.FileProcessStatus;
import com.motd.be.shared.aws.enums.S3DirectoryType;
import com.motd.be.shared.aws.enums.UploadFileType;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class FileRestDocsTest extends BaseRestDocsTest {

	@Test
	void Presigned_url_발급_문서화() throws Exception {
		authenticationSetUp();

		FileUploadRequest requestBody = FileUploadRequest.builder()
			.directoryType(S3DirectoryType.PORTFOLIO.name())
			.fileType(UploadFileType.IMAGE.name())
			.fileExtension(UploadFileType.IMAGE.getAllowedExtensions()
				.iterator()
				.next())
			.fileName("test")
			.fileSize("20MB")
			.build();

		willReturn(FileUploadResponse.builder()
			.presignedUrl(PRESIGNED_URL)
			.imageId(ID)
			.cdnUrl(CDN_URL_STR)
			.build())
			.given(fileFacade).createPresignedUrl(any(Long.class), any(FileUploadRequest.class));

		mockMvc.perform(post("/api/files/presigned-url")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestBody))) // ✅ JSON 변환 처리
			.andExpect(status().isCreated())
			.andDo(document("file-create-presigned-url",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR)
						.description("HttpOnly refreshToken 쿠키")
				),

				requestFields(
					fieldWithPath("directoryType").type(JsonFieldType.STRING)
						.attributes(enumFormat(S3DirectoryType.class, Enum::name))
						.description("presigned-url 발급을 원하는 컨텐츠 타입 (POPUP, banner 는 현재 presigned url 발급 불가)"),

					fieldWithPath("fileType").type(JsonFieldType.STRING)
						.attributes(enumFormat(UploadFileType.class, Enum::name))
						.description("업로드할 파일 유형 (미입력 시 IMAGE)"),

					fieldWithPath("fileExtension").type(JsonFieldType.STRING)
						.attributes(
							key("format").value(
								UploadFileType.IMAGE.name() + "("
									+ String.join(", ", UploadFileType.IMAGE.getAllowedExtensions())
									+ "), "
									+ UploadFileType.DOCUMENT.name() + "("
									+ String.join(", ", UploadFileType.DOCUMENT.getAllowedExtensions())
									+ ")"
							)
						)
						.description("업로드할 파일 유형 (미입력 시 IMAGE)"),

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
					.tag("🔥 이미지·파일 업로드 API")
					.summary("이미지 및 파일 업로드용 Presigned-url 발급 API")
					.description("이미지 또는 파일 업로드용 Presigned-url 발급 API")
					.build()
				)
			));
	}

	@Test
	void 이미지_삭제_문서화() throws Exception {
		authenticationSetUp();

		FileDeleteRequest requestBody = FileDeleteRequest.builder()
			.fileIds(Arrays.asList(ID, ID + 1))
			.directoryType(S3DirectoryType.PORTFOLIO.name())
			.build();

		willDoNothing()
			.given(fileFacade).deleteByIds(any(Long.class), any(FileDeleteRequest.class));

		mockMvc.perform(delete("/api/files")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestBody))) // ✅ JSON 변환 처리
			.andExpect(status().isNoContent())
			.andDo(document("file-delete",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR)
						.description("HttpOnly refreshToken 쿠키")
				),

				requestFields(
					fieldWithPath("fileIds").type(JsonFieldType.ARRAY).description("이미지 또는 파일 ID 목록"),
					fieldWithPath("directoryType").type(JsonFieldType.STRING)
						.attributes(enumFormat(S3DirectoryType.class, Enum::name))
						.description("이미지 삭제를 원하는 컨텐츠 타입")
				),

				resource(builder()
					.tag("🔥 이미지·파일 업로드 API")
					.summary("이미지 삭제 API")
					.description("이미지 삭제 API")
					.build()
				)
			));
	}

	@Test
	void 이미지_처리_상태_조회_문서화() throws Exception {
		authenticationSetUp();

		FileProcessStatusRequest requestBody = FileProcessStatusRequest.builder()
			.fileId(ID)
			.directoryType(S3DirectoryType.PORTFOLIO.name())
			.build();

		willReturn(FileProcessStatusResponse.builder()
			.fileId(ID)
			.directoryType(S3DirectoryType.PORTFOLIO.name())
			.processStatus(FileProcessStatus.PROCESSED)
			.processed(true)
			.build())
			.given(fileFacade).getProcessStatus(any(Long.class), any(FileProcessStatusRequest.class));

		mockMvc.perform(post("/api/files/process-status")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestBody)))
			.andExpect(status().isOk())
			.andDo(document("file-get-process-status",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR)
						.description("HttpOnly refreshToken 쿠키")
				),

				requestFields(
					fieldWithPath("fileId").type(JsonFieldType.NUMBER)
						.description("파일 ID"),
					fieldWithPath("directoryType").type(JsonFieldType.STRING)
						.attributes(enumFormat(S3DirectoryType.class, Enum::name))
						.description("파일이 저장된 디렉토리 타입")
				),

				responseFields(
					fieldWithPath("fileId").type(JsonFieldType.NUMBER)
						.description("파일 ID"),
					fieldWithPath("directoryType").type(JsonFieldType.STRING)
						.attributes(enumFormat(S3DirectoryType.class, Enum::name))
						.description("파일이 저장된 디렉토리 타입"),
					fieldWithPath("processStatus").type(JsonFieldType.STRING)
						.attributes(enumFormat(FileProcessStatus.class, Enum::name))
						.description("파일 처리 상태"),
					fieldWithPath("processed").type(JsonFieldType.BOOLEAN)
						.description("처리 완료 여부")
				),

				resource(builder()
					.tag("🔥 이미지·파일 업로드 API")
					.summary("파일 처리 상태 조회 API")
					.description("업로드된 파일의 처리 상태를 조회하는 API")
					.build()
				)
			));
	}

	@Test
	void 파일_처리_상태_업데이트_문서화() throws Exception {
		FileUpdateProcessRequest requestBody = FileUpdateProcessRequest.builder()
			.fileKey("portfolio/test-file-key")
			.processStatus(FileProcessStatus.PROCESSED)
			.build();

		willDoNothing()
			.given(fileFacade).updateProcessStatus(anyString(), any(FileUpdateProcessRequest.class));

		mockMvc.perform(post("/api/files/processed")
				.header(X_API_KEY, "test-api-key")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestBody)))
			.andExpect(status().isNoContent())
			.andDo(document("file-update-process-status",
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
					.tag("🔥 이미지·파일 업로드 API")
					.summary("파일 처리 상태 업데이트 API (Lambda 콜백)")
					.description("Lambda에서 파일 처리 완료 후 호출하는 파일 처리 상태 업데이트 API")
					.build()
				)
			));
	}

}
