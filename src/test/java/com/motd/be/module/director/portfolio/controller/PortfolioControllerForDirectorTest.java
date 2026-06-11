package com.motd.be.module.director.portfolio.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.common.constants.ValidationConstants.*;
import static com.motd.be.common.constants.ValidationMessages.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.HandlerException;
import com.motd.be.exception.exceptions.PortfolioException;
import com.motd.be.exception.exceptions.PortfolioFileException;
import com.motd.be.module.director.portfolio.dto.request.PortfolioSaveAndUpdateRequestForDirector;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.location.entity.LocationType;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.Role;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.notification.entity.Notification;
import com.motd.be.module.member.notification.entity.NotificationReceiverType;
import com.motd.be.module.member.notification.entity.NotificationType;
import com.motd.be.module.member.portfolio.entity.Portfolio;
import com.motd.be.module.member.portfolio_file.entity.PortfolioFile;
import com.motd.be.module.member.sse.SseEventType;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class PortfolioControllerForDirectorTest extends BaseIntegrationTest {

	@Test
	@DisplayName("디렉터는 포트폴리오를 저장할 수 있다.")
	void savePortfolio() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		PortfolioFile portfolioImage1 = portfolioFileProvider.save(member, null, Boolean.TRUE);
		PortfolioFile portfolioImage2 = portfolioFileProvider.save(member, null, Boolean.FALSE);
		PortfolioFile portfolioImage3 = portfolioFileProvider.save(member, null, Boolean.FALSE);

		//지역 저장
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		directorLocationMappingProvider.save(directorInfo, cityLocation);

		Long thumbnailImageId = portfolioImage1.getId();
		List<Long> fileIds = Arrays.asList(portfolioImage2.getId(), portfolioImage1.getId(), portfolioImage3.getId());

		PortfolioSaveAndUpdateRequestForDirector request = PortfolioSaveAndUpdateRequestForDirector.builder()
			.title(TITLE_STR)
			.content(CONTENT_STR)
			.directorServiceId(directorService2.getId())
			.fileIds(fileIds)
			.thumbnailImageId(thumbnailImageId)
			.price(AUTO_PRICE)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/my/portfolios")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		entityManager.flush();
		entityManager.clear();

		// 실제 저장된 포트폴리오 및 이미지 검증
		List<Portfolio> portfolios = portfolioProvider.findAll();

		assertThat(portfolios).hasSize(1);

		Portfolio savedPortfolio = portfolios.get(0);
		assertThat(savedPortfolio.getTitle()).isEqualTo(TITLE_STR);
		assertThat(savedPortfolio.getContent()).isEqualTo(CONTENT_STR);

		List<PortfolioFile> portfolioImages = savedPortfolio.getFiles();
		assertThat(portfolioImages).hasSize(3);

		// portfolio 가 잘 매핑 되었는지 검증
		portfolioImages.forEach(portfolioImage -> {
			assertThat(portfolioImage.getPortfolio().getId()).isEqualTo(savedPortfolio.getId());
		});

		portfolioImages.forEach(portfolioImage -> {
			if (portfolioImage.getId().equals(portfolioImage1.getId())) {
				assertThat(portfolioImage.getIsThumbnailImage()).isTrue();
			}
		});

		// 디렉터의 정보가 업데이트 되었는지 확인한다.
		DirectorInfo updatedDirectorInfo = directorInfoProvider.findById(member.getDirectorInfo().getId());
		assertThat(updatedDirectorInfo.getIsPortfolioExist()).isTrue();

		// 포트폴리오 이미지의 순서가 보장되는지 확인한다. (2,1,3 순서)
		assertThat(portfolioImages.get(0).getId()).isEqualTo(portfolioImage2.getId());
		assertThat(portfolioImages.get(1).getId()).isEqualTo(portfolioImage1.getId());
		assertThat(portfolioImages.get(2).getId()).isEqualTo(portfolioImage3.getId());
	}

	@Test
	@DisplayName("디렉터는 포트폴리오를 저장할 수 있다. (파일이 제한갯수 이상일때)")
	void savePortfolioWithExceededFileLimitCount() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		List<PortfolioFile> files = new ArrayList<>();
		for (int i = 0; i < PORTFOLIO_MAX_IMAGE_COUNT + 1; i++) {
			PortfolioFile portfolioFile = portfolioFileProvider.save(member, null, i == 0);
			files.add(portfolioFile);
		}

		//지역 저장
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		directorLocationMappingProvider.save(directorInfo, cityLocation);

		Long thumbnailImageId = files.get(0).getId();

		PortfolioSaveAndUpdateRequestForDirector request = PortfolioSaveAndUpdateRequestForDirector.builder()
			.title(TITLE_STR)
			.content(CONTENT_STR)
			.directorServiceId(directorService2.getId())
			.fileIds(files.stream().map(PortfolioFile::getId).toList())
			.thumbnailImageId(thumbnailImageId)
			.price(AUTO_PRICE)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/my/portfolios")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(PORTFOLIO_IMAGE_MAX_COUNT_MSG))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

	@Test
	@DisplayName("디렉터는 포트폴리오를 저장할 수 있다.(디렉터를 즐겨찾기한 회원이 다수 존재할때)")
	void savePortfolioWhenFavoritedMemberExist() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		PortfolioFile portfolioImage1 = portfolioFileProvider.save(director, null, Boolean.TRUE);
		PortfolioFile portfolioImage2 = portfolioFileProvider.save(director, null, Boolean.FALSE);
		PortfolioFile portfolioImage3 = portfolioFileProvider.save(director, null, Boolean.FALSE);

		// 즐겨찾기 회원 저장
		for (int i = 0; i < 10; i++) {
			Member favoritingMember = memberProvider.saveMember(SignInPlatform.KAKAO);
			memberDirectorFavoriteProvider.save(favoritingMember, director);
		}

		//지역 저장
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		directorLocationMappingProvider.save(directorInfo, cityLocation);

		Long thumbnailImageId = portfolioImage1.getId();
		List<Long> fileIds = Arrays.asList(portfolioImage2.getId(), portfolioImage1.getId(), portfolioImage3.getId());

		PortfolioSaveAndUpdateRequestForDirector request = PortfolioSaveAndUpdateRequestForDirector.builder()
			.title(TITLE_STR)
			.content(CONTENT_STR)
			.directorServiceId(directorService2.getId())
			.fileIds(fileIds)
			.thumbnailImageId(thumbnailImageId)
			.price(AUTO_PRICE)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/my/portfolios")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		entityManager.flush();
		entityManager.clear();

		// 실제 저장된 포트폴리오 및 이미지 검증
		List<Portfolio> portfolios = portfolioProvider.findAll();

		assertThat(portfolios).hasSize(1);

		Portfolio savedPortfolio = portfolios.get(0);
		assertThat(savedPortfolio.getTitle()).isEqualTo(TITLE_STR);
		assertThat(savedPortfolio.getContent()).isEqualTo(CONTENT_STR);

		List<PortfolioFile> portfolioImages = savedPortfolio.getFiles();
		assertThat(portfolioImages).hasSize(3);

		// portfolio 가 잘 매핑 되었는지 검증
		portfolioImages.forEach(portfolioImage -> {
			assertThat(portfolioImage.getPortfolio().getId()).isEqualTo(savedPortfolio.getId());
		});

		portfolioImages.forEach(portfolioImage -> {
			if (portfolioImage.getId().equals(portfolioImage1.getId())) {
				assertThat(portfolioImage.getIsThumbnailImage()).isTrue();
			}
		});

		// 디렉터의 정보가 업데이트 되었는지 확인한다.
		DirectorInfo updatedDirectorInfo = directorInfoProvider.findById(director.getDirectorInfo().getId());
		assertThat(updatedDirectorInfo.getIsPortfolioExist()).isTrue();

		// 포트폴리오 이미지의 순서가 보장되는지 확인한다. (2,1,3 순서)
		assertThat(portfolioImages.get(0).getId()).isEqualTo(portfolioImage2.getId());
		assertThat(portfolioImages.get(1).getId()).isEqualTo(portfolioImage1.getId());
		assertThat(portfolioImages.get(2).getId()).isEqualTo(portfolioImage3.getId());

		// notification 이 10 개 저장되었는지 검증
		List<Notification> notifications = notificationProvider.findAll();

		assertThat(notifications).hasSize(10);
		assertThat(notifications.get(0).getReceiverType()).isEqualTo(NotificationReceiverType.MEMBER);
		assertThat(notifications.get(0).getType()).isEqualTo(NotificationType.FAVORITE_PORTFOLIO_UPLOADED);

		// SSE 발행 검증 (REFRESH_NOTIFICATION_COUNT)
		verify(sseService, atLeastOnce()).refreshNotificationCount(
			argThat(payload ->
				payload.getEventName() == SseEventType.REFRESH_NOTIFICATION_COUNT &&
					Objects.equals(payload.getReceiverRole(), Role.MEMBER)));
	}

	@Test
	@DisplayName("디렉터는 포트폴리오를 저장할 수 있다. (디렉터가 아닌 일반 회원이 포트폴리오 저장요청을 할때)")
	void savePortfolioWithMember() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		PortfolioFile portfolioImage1 = portfolioFileProvider.save(member, null, Boolean.TRUE);
		PortfolioFile portfolioImage2 = portfolioFileProvider.save(member, null, Boolean.FALSE);
		PortfolioFile portfolioImage3 = portfolioFileProvider.save(member, null, Boolean.FALSE);

		Long thumbnailImageId = portfolioImage1.getId();
		List<Long> fileIds = Arrays.asList(portfolioImage1.getId(), portfolioImage2.getId(), portfolioImage3.getId());

		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		PortfolioSaveAndUpdateRequestForDirector request = PortfolioSaveAndUpdateRequestForDirector.builder()
			.title(TITLE_STR)
			.content(CONTENT_STR)
			.directorServiceId(directorService2.getId())
			.fileIds(fileIds)
			.thumbnailImageId(thumbnailImageId)
			.price(AUTO_PRICE)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/my/portfolios")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(HandlerException.FORBIDDEN.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(HandlerException.FORBIDDEN.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.FORBIDDEN.getCode()));
	}

	@Test
	@DisplayName("디렉터는 포트폴리오를 저장할 수 있다. (이미지 갯수 불일치시)")
	void savePortfolio_invalidImageCount() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		PortfolioFile portfolioImage1 = portfolioFileProvider.save(member, null, Boolean.TRUE);
		PortfolioFile portfolioImage2 = portfolioFileProvider.save(member, null, Boolean.FALSE);
		PortfolioFile portfolioImage3 = portfolioFileProvider.save(member, null, Boolean.FALSE);

		//지역 저장
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		directorLocationMappingProvider.save(directorInfo, cityLocation);

		Long thumbnailImageId = portfolioImage1.getId();
		List<Long> fileIds = Arrays.asList(portfolioImage1.getId(), portfolioImage2.getId(), portfolioImage3.getId(),
			999L);

		PortfolioSaveAndUpdateRequestForDirector request = PortfolioSaveAndUpdateRequestForDirector.builder()
			.title(TITLE_STR)
			.content(CONTENT_STR)
			.directorServiceId(directorService2.getId())
			.fileIds(fileIds)
			.thumbnailImageId(thumbnailImageId)
			.price(AUTO_PRICE)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/my/portfolios")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(PortfolioFileException.INVALID_IMAGE_COUNT.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(PortfolioFileException.INVALID_IMAGE_COUNT.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(PortfolioFileException.INVALID_IMAGE_COUNT.getCode()));
	}

	@Test
	@DisplayName("디렉터는 포트폴리오를 저장할 수 있다. (이미 매핑된 이미지 존재시)")
	void savePortfolio_AlreadyMappedImageExist() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		Portfolio portfolio = portfolioProvider.save(directorService2, directorInfo);

		PortfolioFile portfolioImage1 = portfolioFileProvider.save(member, portfolio, Boolean.TRUE);
		PortfolioFile portfolioImage2 = portfolioFileProvider.save(member, null, Boolean.FALSE);
		PortfolioFile portfolioImage3 = portfolioFileProvider.save(member, null, Boolean.FALSE);

		//지역 저장
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		directorLocationMappingProvider.save(directorInfo, cityLocation);

		Long thumbnailImageId = portfolioImage1.getId();
		List<Long> fileIds = Arrays.asList(portfolioImage1.getId(), portfolioImage2.getId(), portfolioImage3.getId(),
			999L);

		PortfolioSaveAndUpdateRequestForDirector request = PortfolioSaveAndUpdateRequestForDirector.builder()
			.title(TITLE_STR)
			.content(CONTENT_STR)
			.directorServiceId(directorService2.getId())
			.fileIds(fileIds)
			.thumbnailImageId(thumbnailImageId)
			.price(AUTO_PRICE)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/my/portfolios")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(PortfolioFileException.INVALID_IMAGE_COUNT.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(PortfolioFileException.INVALID_IMAGE_COUNT.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(PortfolioFileException.INVALID_IMAGE_COUNT.getCode()));
	}

	@Test
	@DisplayName("디렉터는 포트폴리오를 저장할 수 있다. (이미지 소유권 불일치시 )")
	void savePortfolio_invalidImageOwner() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Member otherMember = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		PortfolioFile portfolioImage1 = portfolioFileProvider.save(member, null, Boolean.TRUE);
		PortfolioFile portfolioImage2 = portfolioFileProvider.save(member, null, Boolean.FALSE);
		PortfolioFile portfolioImage3 = portfolioFileProvider.save(member, null, Boolean.FALSE);
		PortfolioFile otherTempImage = portfolioFileProvider.save(otherMember, null, Boolean.FALSE);

		//지역 저장
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		directorLocationMappingProvider.save(directorInfo, cityLocation);

		Long thumbnailImageId = portfolioImage1.getId();
		List<Long> fileIds = Arrays.asList(portfolioImage1.getId(), portfolioImage2.getId(), otherTempImage.getId());

		PortfolioSaveAndUpdateRequestForDirector request = PortfolioSaveAndUpdateRequestForDirector.builder()
			.title(TITLE_STR)
			.content(CONTENT_STR)
			.directorServiceId(directorService2.getId())
			.fileIds(fileIds)
			.thumbnailImageId(thumbnailImageId)
			.price(AUTO_PRICE)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/my/portfolios")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(PortfolioException.IMAGE_NOT_OWNED.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(PortfolioException.IMAGE_NOT_OWNED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(PortfolioException.IMAGE_NOT_OWNED.getCode()));
	}

	@Test
	@DisplayName("포트폴리오를 수정할 수 있다. (썸네일 이미지를 바꾸는 경우")
	void updatePortfolioWithChangingThumbnailImage() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		// director service 저장
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		//지역 저장
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		directorLocationMappingProvider.save(directorInfo, cityLocation);

		// portfolio 저장
		Portfolio portfolio = portfolioProvider.save(directorService2, directorInfo);

		// 포트폴리오 이미지 저장
		PortfolioFile portfolioImage1 = portfolioFileProvider.save(member, portfolio, Boolean.TRUE);
		PortfolioFile portfolioImage2 = portfolioFileProvider.save(member, portfolio, Boolean.FALSE);
		PortfolioFile portfolioImage3 = portfolioFileProvider.save(member, portfolio, Boolean.FALSE);

		// 포트폴리오 새로운 이미지 저장
		PortfolioFile newImage1 = portfolioFileProvider.save(member, null, Boolean.FALSE);

		Long thumbnailImageId = newImage1.getId();
		List<Long> fileIds = Arrays.asList(newImage1.getId(), portfolioImage2.getId(), portfolioImage3.getId());

		PortfolioSaveAndUpdateRequestForDirector saveRequest = PortfolioSaveAndUpdateRequestForDirector.builder()
			.title(UPDATED_TITLE_STR)
			.content(UPDATED_CONTENT_STR)
			.directorServiceId(directorService2.getId())
			.fileIds(fileIds)
			.thumbnailImageId(thumbnailImageId)
			.price(AUTO_PRICE)
			.build();

		entityManager.flush();
		entityManager.clear();

		//when
		mockMvc.perform(MockMvcRequestBuilders.put("/api/directors/my/portfolios/{portfolioId}", portfolio.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(saveRequest)))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// 실제 수정된 포트폴리오 및 이미지 검증
		Portfolio updatedPortfolio = portfolioProvider.findById(portfolio.getId());

		assertThat(updatedPortfolio.getTitle()).isEqualTo(UPDATED_TITLE_STR);
		assertThat(updatedPortfolio.getContent()).isEqualTo(UPDATED_CONTENT_STR);

		List<PortfolioFile> portfolioImages = updatedPortfolio.getFiles();

		// portfolioImage1 삭제되었는지, newImage1 이 추가되었는지 검증한다.
		assertThat(portfolioImages).hasSize(3);
		assertThat(portfolioImages).extracting(ID_STR)
			.containsExactlyInAnyOrder(portfolioImage2.getId(), portfolioImage3.getId(), newImage1.getId());

		// 썸네일 이미지가 newImage1 로 변경되었는지 검증
		portfolioImages.forEach(portfolioImage -> {
			if (portfolioImage.getId().equals(newImage1.getId())) {
				assertThat(portfolioImage.getIsThumbnailImage()).isTrue();
			} else {
				assertThat(portfolioImage.getIsThumbnailImage()).isFalse();
			}
		});

		// 이미지 순서 검증 ( newImage1, portfolioImage2, portfolioImage3 )
		assertThat(portfolioImages.get(0).getId()).isEqualTo(newImage1.getId());
		assertThat(portfolioImages.get(1).getId()).isEqualTo(portfolioImage2.getId());
		assertThat(portfolioImages.get(2).getId()).isEqualTo(portfolioImage3.getId());
	}

	@Test
	@DisplayName("포트폴리오를 수정할 수 있다. (기존 이미지 중 하나를 썸네일로 변경하는 경우)")
	void updatePortfolioWithChangingThumbnailToExistingImage() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		// director service 저장
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		//지역 저장
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		directorLocationMappingProvider.save(directorInfo, cityLocation);

		// portfolio 저장
		Portfolio portfolio = portfolioProvider.save(directorService2, directorInfo);

		// 포트폴리오 이미지 저장 (portfolioImage1이 기존 썸네일)
		PortfolioFile portfolioImage1 = portfolioFileProvider.save(member, portfolio, Boolean.TRUE);
		PortfolioFile portfolioImage2 = portfolioFileProvider.save(member, portfolio, Boolean.FALSE);
		PortfolioFile portfolioImage3 = portfolioFileProvider.save(member, portfolio, Boolean.FALSE);

		// 기존 이미지2를 썸네일로 변경 (새 이미지 추가 없이)
		Long thumbnailImageId = portfolioImage2.getId();
		List<Long> fileIds = Arrays.asList(portfolioImage1.getId(), portfolioImage2.getId(), portfolioImage3.getId());

		PortfolioSaveAndUpdateRequestForDirector saveRequest = PortfolioSaveAndUpdateRequestForDirector.builder()
			.title(UPDATED_TITLE_STR)
			.content(UPDATED_CONTENT_STR)
			.directorServiceId(directorService2.getId())
			.fileIds(fileIds)
			.thumbnailImageId(thumbnailImageId)
			.price(AUTO_PRICE)
			.build();

		entityManager.flush();
		entityManager.clear();

		//when
		mockMvc.perform(MockMvcRequestBuilders.put("/api/directors/my/portfolios/{portfolioId}", portfolio.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(saveRequest)))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// 실제 수정된 포트폴리오 및 이미지 검증
		Portfolio updatedPortfolio = portfolioProvider.findById(portfolio.getId());

		assertThat(updatedPortfolio.getTitle()).isEqualTo(UPDATED_TITLE_STR);
		assertThat(updatedPortfolio.getContent()).isEqualTo(UPDATED_CONTENT_STR);

		List<PortfolioFile> portfolioImages = updatedPortfolio.getFiles();

		// 모든 이미지가 유지되었는지 검증
		assertThat(portfolioImages).hasSize(3);
		assertThat(portfolioImages).extracting(ID_STR)
			.containsExactlyInAnyOrder(portfolioImage1.getId(), portfolioImage2.getId(), portfolioImage3.getId());

		// 썸네일 이미지가 portfolioImage2로 변경되었는지 검증
		portfolioImages.forEach(portfolioImage -> {
			if (portfolioImage.getId().equals(portfolioImage2.getId())) {
				assertThat(portfolioImage.getIsThumbnailImage()).isTrue();
			} else {
				assertThat(portfolioImage.getIsThumbnailImage()).isFalse();
			}
		});

		// 이미지 순서 검증 (portfolioImage1, portfolioImage2, portfolioImage3)
		assertThat(portfolioImages.get(0).getId()).isEqualTo(portfolioImage1.getId());
		assertThat(portfolioImages.get(1).getId()).isEqualTo(portfolioImage2.getId());
		assertThat(portfolioImages.get(2).getId()).isEqualTo(portfolioImage3.getId());
	}

	@Test
	@DisplayName("포트폴리오를 수정할 수 있다. (썸네일 이미지가 images 안에 있지 않는 경우")
	void updatePortfolioWhenThumbnailFileNotExistInFileIds() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		// director service 저장
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		//지역 저장
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		directorLocationMappingProvider.save(directorInfo, cityLocation);

		// portfolio 저장
		Portfolio portfolio = portfolioProvider.save(directorService2, directorInfo);

		// 포트폴리오 이미지 저장
		PortfolioFile portfolioImage1 = portfolioFileProvider.save(member, portfolio, Boolean.TRUE);
		PortfolioFile portfolioImage2 = portfolioFileProvider.save(member, portfolio, Boolean.FALSE);
		PortfolioFile portfolioImage3 = portfolioFileProvider.save(member, portfolio, Boolean.FALSE);

		// 포트폴리오 새로운 이미지 저장
		PortfolioFile newImage1 = portfolioFileProvider.save(member, null, Boolean.FALSE);

		List<Long> fileIds = Arrays.asList(newImage1.getId(), portfolioImage2.getId(), portfolioImage3.getId());

		PortfolioSaveAndUpdateRequestForDirector saveRequest = PortfolioSaveAndUpdateRequestForDirector.builder()
			.title(UPDATED_TITLE_STR)
			.content(UPDATED_CONTENT_STR)
			.directorServiceId(directorService2.getId())
			.fileIds(fileIds)
			.thumbnailImageId(9999L)
			.price(AUTO_PRICE)
			.build();

		entityManager.flush();
		entityManager.clear();

		//when
		mockMvc.perform(MockMvcRequestBuilders.put("/api/directors/my/portfolios/{portfolioId}", portfolio.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(saveRequest)))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(PortfolioFileException.INVALID_THUMBNAIL_IMAGE.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(PortfolioFileException.INVALID_THUMBNAIL_IMAGE.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(PortfolioFileException.INVALID_THUMBNAIL_IMAGE.getCode()));
	}

	@Test
	@DisplayName("포트폴리오를 수정할 수 있다. (images 가 비어있는 경우")
	void updatePortfolioWhenFileIdsEmpty() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		// director service 저장
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		//지역 저장
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		directorLocationMappingProvider.save(directorInfo, cityLocation);

		// portfolio 저장
		Portfolio portfolio = portfolioProvider.save(directorService2, directorInfo);

		// 포트폴리오 이미지 저장
		PortfolioFile portfolioImage1 = portfolioFileProvider.save(member, portfolio, Boolean.TRUE);
		PortfolioFile portfolioImage2 = portfolioFileProvider.save(member, portfolio, Boolean.FALSE);
		PortfolioFile portfolioImage3 = portfolioFileProvider.save(member, portfolio, Boolean.FALSE);

		// 포트폴리오 새로운 이미지 저장
		PortfolioFile newImage1 = portfolioFileProvider.save(member, null, Boolean.FALSE);

		List<Long> fileIds = Arrays.asList();

		PortfolioSaveAndUpdateRequestForDirector saveRequest = PortfolioSaveAndUpdateRequestForDirector.builder()
			.title(UPDATED_TITLE_STR)
			.content(UPDATED_CONTENT_STR)
			.directorServiceId(directorService2.getId())
			.fileIds(fileIds)
			.thumbnailImageId(9999L)
			.price(AUTO_PRICE)
			.build();

		entityManager.flush();
		entityManager.clear();

		//when
		mockMvc.perform(MockMvcRequestBuilders.put("/api/directors/my/portfolios/{portfolioId}", portfolio.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(saveRequest)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

	@Test
	@DisplayName("포트폴리오를 수정할 수 있다. (본인이 작성한 포트폴리오가 아닐 경우")
	void updatePortfolioWhenPortfolioNotOwned() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorInfo otherDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member otherMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, otherDirectorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		// director service 저장
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		directorServiceMappingProvider.save(otherDirectorInfo, directorService2);
		directorServiceMappingProvider.save(directorInfo, directorService2);

		//지역 저장
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		directorLocationMappingProvider.save(directorInfo, cityLocation);
		directorLocationMappingProvider.save(otherDirectorInfo, cityLocation);

		// portfolio 저장
		Portfolio portfolio = portfolioProvider.save(directorService2, otherDirectorInfo);

		// 포트폴리오 이미지 저장
		PortfolioFile portfolioImage1 = portfolioFileProvider.save(otherMember, portfolio, Boolean.TRUE);
		PortfolioFile portfolioImage2 = portfolioFileProvider.save(otherMember, portfolio, Boolean.FALSE);
		PortfolioFile portfolioImage3 = portfolioFileProvider.save(otherMember, portfolio, Boolean.FALSE);

		// 포트폴리오 새로운 이미지 저장
		PortfolioFile newImage1 = portfolioFileProvider.save(member, null, Boolean.FALSE);

		List<Long> fileIds = Arrays.asList(newImage1.getId(), portfolioImage2.getId(), portfolioImage3.getId());

		PortfolioSaveAndUpdateRequestForDirector saveRequest = PortfolioSaveAndUpdateRequestForDirector.builder()
			.title(UPDATED_TITLE_STR)
			.content(UPDATED_CONTENT_STR)
			.directorServiceId(directorService2.getId())
			.fileIds(fileIds)
			.thumbnailImageId(9999L)
			.price(AUTO_PRICE)
			.build();

		entityManager.flush();
		entityManager.clear();

		//when
		mockMvc.perform(MockMvcRequestBuilders.put("/api/directors/my/portfolios/{portfolioId}", portfolio.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(saveRequest)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(PortfolioException.NO_AUTHORITY.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(PortfolioException.NO_AUTHORITY.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(PortfolioException.NO_AUTHORITY.getCode()));
	}

	@Test
	@DisplayName("포트폴리오를 삭제할 수 있다. (포트폴리오가 2개인상태에서 1개로 삭제했을때)")
	void deleteWhenMoreThanOnePortfolioExist() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithProfileCompleteness(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, false, true, false, false);
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		// director service 저장
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		//지역 저장
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		directorLocationMappingProvider.save(directorInfo, cityLocation);

		// portfolio 저장
		Portfolio portfolio = portfolioProvider.save(directorService2, directorInfo);
		Portfolio portfolio2 = portfolioProvider.save(directorService2, directorInfo);

		// 포트폴리오 이미지 저장
		PortfolioFile portfolioImage1 = portfolioFileProvider.save(member, portfolio, Boolean.TRUE);
		PortfolioFile portfolioImage2 = portfolioFileProvider.save(member, portfolio, Boolean.FALSE);
		PortfolioFile portfolioImage3 = portfolioFileProvider.save(member, portfolio, Boolean.FALSE);

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(MockMvcRequestBuilders.delete("/api/directors/my/portfolios/{portfolioId}", portfolio.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// 삭제 상태로 변경되었는지 확인
		Portfolio deletedPortfolio = portfolioProvider.findById(portfolio.getId());
		assertThat(deletedPortfolio.getIsDeleted()).isTrue();

		List<PortfolioFile> portfolioImages = portfolioFileProvider.findAllByPortfolioId(portfolio.getId());
		assertThat(portfolioImages).hasSize(0);

		// 디렉터의 정보가 업데이트 되었는지 확인한다.
		DirectorInfo updatedDirectorInfo = directorInfoProvider.findById(member.getDirectorInfo().getId());
		assertThat(updatedDirectorInfo.getIsPortfolioExist()).isTrue();
	}

	@Test
	@DisplayName("포트폴리오를 삭제할 수 있다. (포트폴리오가 1개인상태에서 삭제했을때)")
	void deleteWhenOnePortfolioExist() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithProfileCompleteness(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, false, true, false, false);
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		// director service 저장
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		//지역 저장
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		directorLocationMappingProvider.save(directorInfo, cityLocation);

		// portfolio 저장
		Portfolio portfolio = portfolioProvider.save(directorService2, directorInfo);

		// 포트폴리오 이미지 저장
		PortfolioFile portfolioImage1 = portfolioFileProvider.save(member, portfolio, Boolean.TRUE);
		PortfolioFile portfolioImage2 = portfolioFileProvider.save(member, portfolio, Boolean.FALSE);
		PortfolioFile portfolioImage3 = portfolioFileProvider.save(member, portfolio, Boolean.FALSE);

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(MockMvcRequestBuilders.delete("/api/directors/my/portfolios/{portfolioId}", portfolio.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// 삭제 상태로 변경되었는지 확인
		Portfolio deletedPortfolio = portfolioProvider.findById(portfolio.getId());
		assertThat(deletedPortfolio.getIsDeleted()).isTrue();

		List<PortfolioFile> portfolioImages = portfolioFileProvider.findAllByPortfolioId(portfolio.getId());
		assertThat(portfolioImages).hasSize(0);

		// 디렉터의 정보가 업데이트 되었는지 확인한다.
		DirectorInfo updatedDirectorInfo = directorInfoProvider.findById(member.getDirectorInfo().getId());
		assertThat(updatedDirectorInfo.getIsPortfolioExist()).isFalse();
	}

	@Test
	@DisplayName("포트폴리오를 삭제할 수 있다. (자신이 올린 포트폴리오가 아닐때)")
	void deleteWithNotOwnedPortfolio() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorInfo otherDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member otherMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, otherDirectorInfo);

		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		// director service 저장
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		directorServiceMappingProvider.save(otherDirectorInfo, directorService2);

		//지역 저장
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		directorLocationMappingProvider.save(directorInfo, cityLocation);
		directorLocationMappingProvider.save(otherDirectorInfo, cityLocation);

		// portfolio 저장
		Portfolio portfolio = portfolioProvider.save(directorService2, otherDirectorInfo);

		// 포트폴리오 이미지 저장
		PortfolioFile portfolioImage1 = portfolioFileProvider.save(otherMember, portfolio, Boolean.TRUE);
		PortfolioFile portfolioImage2 = portfolioFileProvider.save(otherMember, portfolio, Boolean.FALSE);
		PortfolioFile portfolioImage3 = portfolioFileProvider.save(otherMember, portfolio, Boolean.FALSE);

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(MockMvcRequestBuilders.delete("/api/directors/my/portfolios/{portfolioId}", portfolio.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(PortfolioException.NO_AUTHORITY.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(PortfolioException.NO_AUTHORITY.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(PortfolioException.NO_AUTHORITY.getCode()));
	}
}
