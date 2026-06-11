package com.motd.be;

import static com.motd.be.Constants.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.motd.be.common.log.LoggerProvider;
import com.motd.be.common.utils.ActiveProfileUtils;
import com.motd.be.common.utils.CookieUtils;
import com.motd.be.module.admin.admin.service.AdminService;
import com.motd.be.module.admin.admin_file.facade.AdminFileFacadeForAdmin;
import com.motd.be.module.admin.auth.service.AuthAdminService;
import com.motd.be.module.admin.banner.facade.BannerFacadeForAdmin;
import com.motd.be.module.admin.banner.service.BannerServiceForAdmin;
import com.motd.be.module.admin.dashboard.facade.AdminDashboardFacadeForAdmin;
import com.motd.be.module.admin.director_service.facade.DirectorServiceFacadeForAdmin;
import com.motd.be.module.admin.member.facade.MemberFacadeForAdmin;
import com.motd.be.module.admin.popup.facade.PopupAdminFacade;
import com.motd.be.module.admin.popup.service.PopupAdminService;
import com.motd.be.module.admin.portfolio.facade.PortfolioFacadeForAdmin;
import com.motd.be.module.admin.service_request.facade.ServiceRequestFacadeForAdmin;
import com.motd.be.module.admin.service_estimate.facade.ServiceEstimateFacadeForAdmin;
import com.motd.be.module.admin.chat_room.facade.ChatRoomFacadeForAdmin;
import com.motd.be.module.director.consulting_request.facade.ConsultingRequestFacadeForDirector;
import com.motd.be.module.member.consulting_request.facade.ConsultingRequestFacade;
import com.motd.be.module.member.consulting_sheet.facade.ConsultingSheetFacade;
import com.motd.be.module.member.prompt.facade.PromptFacade;
import com.motd.be.shared.ai.provider.AiChatProvider;
import com.motd.be.module.director.banner.facade.BannerFacadeForDirector;
import com.motd.be.module.director.business_registration.facade.BusinessRegistrationFacadeForDirector;
import com.motd.be.module.director.cash.facade.CashFacadeForDirector;
import com.motd.be.module.director.cash_transaction_history.facade.CashTransactionHistoryFacadeForDirector;
import com.motd.be.module.director.chat_room.facade.ChatRoomFacadeForDirector;
import com.motd.be.module.director.director_info.facade.DirectorInfoFacadeForDirector;
import com.motd.be.module.director.director_location_mapping.facade.DirectorLocationMappingFacadeForDirector;
import com.motd.be.module.director.director_profile_detail.facade.DirectorProfileDetailFacadeForDirector;
import com.motd.be.module.director.director_service_mapping.facade.DirectorServiceMappingFacadeForDirector;
import com.motd.be.module.director.member.facade.MemberFacadeForDirector;
import com.motd.be.module.director.notification.facade.NotificationFacadeForDirector;
import com.motd.be.module.director.popup.facade.PopupFacadeForDirector;
import com.motd.be.module.director.portfolio.facade.PortfolioFacadeForDirector;
import com.motd.be.module.director.promotion_code.facade.PromotionCodeFacadeForDirector;
import com.motd.be.module.director.service_estimate.facade.ServiceEstimateFacadeForDirector;
import com.motd.be.module.director.service_estimate_template.facade.ServiceEstimateTemplateFacadeForDirector;
import com.motd.be.module.director.service_request.facade.ServiceRequestFacadeForDirector;
import com.motd.be.module.member.apple_oauth.facade.AppleOauthFacade;
import com.motd.be.module.member.auth.facade.AuthFacade;
import com.motd.be.module.member.auth.service.OAuthResponseHelper;
import com.motd.be.module.member.banner.facade.BannerFacade;
import com.motd.be.module.member.chat_message.facade.ChatMessageFacade;
import com.motd.be.module.member.chat_room.facade.ChatRoomFacade;
import com.motd.be.module.member.director_info.facade.DirectorInfoFacade;
import com.motd.be.module.member.director_profile_detail.facade.DirectorProfileDetailFacade;
import com.motd.be.module.member.director_service.facade.DirectorServiceFacade;
import com.motd.be.module.member.director_service_mapping.facade.DirectorServiceMappingFacade;
import com.motd.be.module.member.fcm_token.facade.FcmTokenFacade;
import com.motd.be.module.member.file.facade.FileFacade;
import com.motd.be.module.member.google_oauth.facade.GoogleOauthFacade;
import com.motd.be.module.member.kakao_oauth.facade.KakaoOauthFacade;
import com.motd.be.module.member.location.facade.LocationFacade;
import com.motd.be.module.member.member.facade.MemberFacade;
import com.motd.be.module.member.member.service.MemberService;
import com.motd.be.module.member.member_block.facade.MemberBlockFacade;
import com.motd.be.module.member.member_director_favorite.facade.MemberDirectorFavoriteFacade;
import com.motd.be.module.member.member_location_mapping.facade.MemberLocationMappingFacade;
import com.motd.be.module.member.member_metadata.facade.MemberMetadataFacade;
import com.motd.be.module.member.notification.facade.NotificationFacade;
import com.motd.be.module.member.popup.facade.PopupFacade;
import com.motd.be.module.member.portfolio.facade.PortfolioFacade;
import com.motd.be.module.member.report.facade.ReportFacade;
import com.motd.be.module.member.review.facade.ReviewFacade;
import com.motd.be.module.member.service_estimate.facade.ServiceEstimateFacade;
import com.motd.be.module.member.service_request.facade.ServiceRequestFacade;
import com.motd.be.module.member.sse.facade.SseFacade;
import com.motd.be.module.member.story.facade.StoryFacade;
import com.motd.be.module.member.time_slot.facade.TimeSlotFacade;
import com.motd.be.redis.domain.brocker.ChatMessagePublisher;
import com.motd.be.redis.domain.repository.RedisMobileOkRepository;
import com.motd.be.shared.aws.service.S3Uploader;
import com.motd.be.shared.mobile_ok.facade.MobileOkFacade;

public abstract class BaseRestDocsTest {

	protected static final HeaderDescriptor AUTH_HEADER =
		headerWithName(AUTHORIZATION_STR)
			.description("Bearer 인증 토큰 (로그인 필요)");
	@Autowired
	protected MockMvc mockMvc;
	@Autowired
	protected ObjectMapper objectMapper;
	@MockitoBean
	protected OAuthResponseHelper oAuthCommonService;
	@MockitoBean
	protected PopupFacade popupFacade;
	@MockitoBean
	protected S3Uploader s3Uploader;
	@MockitoBean
	protected DirectorInfoFacade directorInfoFacade;
	@MockitoBean
	protected MemberFacade memberFacade;
	@MockitoBean
	protected MemberMetadataFacade memberMetadataFacade;
	@MockitoBean
	protected PortfolioFacade portfolioFacade;
	@MockitoBean
	protected LocationFacade locationFacade;
	@MockitoBean
	protected DirectorServiceMappingFacade directorServiceMappingFacade;
	@MockitoBean
	protected DirectorServiceFacade directorServiceFacade;
	@MockitoBean
	protected ServiceRequestFacade serviceRequestFacade;
	@MockitoBean
	protected ServiceEstimateFacade serviceEstimateFacade;
	@MockitoBean
	protected AuthFacade authFacade;
	@MockitoBean
	protected AppleOauthFacade appleOauthFacade;
	@MockitoBean
	protected KakaoOauthFacade kakaoOauthFacade;
	@MockitoBean
	protected GoogleOauthFacade googleOauthFacade;
	@MockitoBean
	protected FileFacade fileFacade;
	@MockitoBean
	protected ChatMessageFacade chatMessageFacade;
	@MockitoBean
	protected ChatRoomFacade chatRoomFacade;
	@MockitoBean
	protected DirectorProfileDetailFacade directorProfileDetailFacade;
	@MockitoBean
	protected SseFacade sseFacade;
	@MockitoBean
	protected ChatMessagePublisher chatMessagePublisher;
	@MockitoBean
	protected StoryFacade storyFacade;
	@MockitoBean
	protected ReviewFacade reviewFacade;
	@MockitoBean
	protected MemberBlockFacade memberBlockFacade;
	@MockitoBean
	protected MemberDirectorFavoriteFacade memberDirectorFavoriteFacade;
	@MockitoBean
	protected ReportFacade reportFacade;
	@MockitoBean
	protected NotificationFacade notificationFacade;
	@MockitoBean
	protected MemberLocationMappingFacade memberLocationMappingFacade;
	@MockitoBean
	protected BusinessRegistrationFacadeForDirector businessRegistrationFacadeForDirector;
	@MockitoBean
	protected TimeSlotFacade timeSlotFacade;
	@MockitoBean
	protected RedisMobileOkRepository redisMobileOkRepository;
	//director
	@MockitoBean
	protected CashTransactionHistoryFacadeForDirector cashTransactionHistoryFacadeForDirector;
	@MockitoBean
	protected CashFacadeForDirector cashFacadeForDirector;
	@MockitoBean
	protected ChatRoomFacadeForDirector chatRoomFacadeForDirector;
	@MockitoBean
	protected DirectorInfoFacadeForDirector directorInfoFacadeForDirector;
	@MockitoBean
	protected DirectorLocationMappingFacadeForDirector directorLocationMappingFacadeForDirector;
	@MockitoBean
	protected DirectorProfileDetailFacadeForDirector directorProfileDetailFacadeForDirector;
	@MockitoBean
	protected DirectorServiceMappingFacadeForDirector directorServiceMappingFacadeForDirector;
	@MockitoBean
	protected MemberFacadeForDirector memberFacadeForDirector;
	@MockitoBean
	protected NotificationFacadeForDirector notificationFacadeForDirector;
	@MockitoBean
	protected PortfolioFacadeForDirector portfolioFacadeForDirector;
	@MockitoBean
	protected ServiceEstimateFacadeForDirector serviceEstimateFacadeForDirector;
	@MockitoBean
	protected ServiceEstimateTemplateFacadeForDirector serviceEstimateTemplateFacadeForDirector;
	@MockitoBean
	protected ServiceRequestFacadeForDirector serviceRequestFacadeForDirector;
	@MockitoBean
	protected BannerFacadeForDirector bannerFacadeForDirector;
	@MockitoBean
	protected BannerFacade bannerFacade;
	@MockitoBean
	protected MobileOkFacade mobileOkFacade;
	@MockitoBean
	protected PromotionCodeFacadeForDirector promotionCodeFacadeForDirector;
	@MockitoBean
	protected FcmTokenFacade fcmTokenFacade;
	//admin
	@MockitoBean
	protected PortfolioFacadeForAdmin portfolioFacadeForAdmin;
	@MockitoBean
	protected AuthAdminService authAdminService;
	@MockitoBean
	protected AdminService adminService;
	@MockitoBean
	protected AdminDashboardFacadeForAdmin adminDashboardFacadeForAdmin;
	@MockitoBean
	protected AdminFileFacadeForAdmin adminFileFacadeForAdmin;
	@MockitoBean
	protected PopupAdminFacade popupAdminFacade;
	@MockitoBean
	protected PopupAdminService popupAdminService;
	@MockitoBean
	protected BannerFacadeForAdmin bannerFacadeForAdmin;
	@MockitoBean
	protected BannerServiceForAdmin bannerServiceForAdmin;
	@MockitoBean
	protected DirectorServiceFacadeForAdmin directorServiceFacadeForAdmin;
	@MockitoBean
	protected MemberFacadeForAdmin memberFacadeForAdmin;
	@MockitoBean
	protected SimpMessageSendingOperations simpMessageSendingOperations;
	@MockitoBean
	protected PopupFacadeForDirector popupFacadeForDirector;
	@MockitoBean
	protected ServiceRequestFacadeForAdmin serviceRequestFacadeForAdmin;
	@MockitoBean
	protected com.motd.be.module.admin.service_estimate.facade.ServiceEstimateFacadeForAdmin serviceEstimateFacadeForAdmin;
	@MockitoBean
	protected ChatRoomFacadeForAdmin chatRoomFacadeForAdmin;
	@MockitoBean
	protected ConsultingRequestFacadeForDirector consultingRequestFacadeForDirector;
	@MockitoBean
	protected com.motd.be.module.director.consulting_sheet.facade.ConsultingSheetFacadeForDirector consultingSheetFacadeForDirector;
	@MockitoBean
	protected ConsultingRequestFacade consultingRequestFacade;
	@MockitoBean
	protected ConsultingSheetFacade consultingSheetFacade;
	@MockitoBean
	protected com.motd.be.module.admin.consulting_sheet.facade.ConsultingSheetFacadeForAdmin consultingSheetFacadeForAdmin;
	@MockitoBean
	protected com.motd.be.module.admin.consulting_request.facade.ConsultingRequestFacadeForAdmin consultingRequestFacadeForAdmin;
	@MockitoBean
	protected PromptFacade promptFacade;
	// 웹 애플리케이션 컨텍스트 및 쿠키 유틸리티 주입
	@Autowired
	protected WebApplicationContext webApplicationContext;
	@Autowired
	protected CookieUtils cookieUtils;
	// 로깅용 mockito bean 추가
	@MockitoBean
	private LoggerProvider loggerProvider;
	@MockitoBean
	private ActiveProfileUtils activeProfileUtil;
	@MockitoBean
	private MemberService memberService;

	@BeforeEach
	void setUp(RestDocumentationContextProvider restDocumentation) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
			.apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
			.build();
	}

	protected void authenticationSetUp() {
		// SecurityContext와 Authentication을 Mocking
		SecurityContext securityContext = mock(SecurityContext.class);
		Authentication authentication = mock(Authentication.class);

		// Principal이 "123" (사용자 ID) 값을 반환하도록 설정
		given(authentication.getPrincipal()).willReturn("1");
		given(securityContext.getAuthentication()).willReturn(authentication);

		// SecurityContextHolder에 Mock SecurityContext 설정
		SecurityContextHolder.setContext(securityContext);
	}

	/**
	 * ✅ 요청과 응답에 공통적으로 적용할 전처리기 (Preprocessors) 설정
	 */
	protected OperationRequestPreprocessor getRequestPreProcessor() {
		return preprocessRequest(prettyPrint(),
			modifyHeaders().remove("Host"));  // 공통적으로 Host 헤더 제거
	}

	protected OperationResponsePreprocessor getResponsePreProcessor() {
		return preprocessResponse(prettyPrint(),
			modifyHeaders()
				.remove("Vary")
				.remove("X-Content-Type-Options")
				.remove("X-XSS-Protection")
				.remove("Cache-Control")
				.remove("Pragma")
				.remove("Expires")
				.remove("X-Frame-Options")
				.remove("Transfer-Encoding")
				.remove("Date")
				.remove("Keep-Alive")
				.remove("Connection")
				.remove("Content-Length")
		);
	}
}
