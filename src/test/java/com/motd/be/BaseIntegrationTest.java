package com.motd.be;

import java.time.Clock;

import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.motd.be.common.adpapter.EventPublisherAdapter;
import com.motd.be.module.member.report_file.repository.ReportFileRepository;
import com.motd.be.module.member.review_file.repository.ReviewFileRepository;
import com.motd.be.module.member.sse.service.SseService;
import com.motd.be.provider.module.admin.AdminProvider;
import com.motd.be.provider.module.admin.PopupAdminProvider;
import com.motd.be.provider.module.member.AppleRefreshTokenProvider;
import com.motd.be.provider.module.member.BannerFileProvider;
import com.motd.be.provider.module.member.BannerProvider;
import com.motd.be.provider.module.member.BusinessRegistrationFileProvider;
import com.motd.be.provider.module.member.BusinessRegistrationProvider;
import com.motd.be.provider.module.member.CashProductProvider;
import com.motd.be.provider.module.member.CashTransactionHistoryProvider;
import com.motd.be.provider.module.member.ChatFileProvider;
import com.motd.be.provider.module.member.ChatMessageProvider;
import com.motd.be.provider.module.member.ChatRoomMemberProvider;
import com.motd.be.provider.module.member.ChatRoomProvider;
import com.motd.be.provider.module.member.ChatRoomServiceEstimateMappingProvider;
import com.motd.be.provider.module.member.CodeUsageHistoryProvider;
import com.motd.be.provider.module.member.ConsultingRequestFileProvider;
import com.motd.be.provider.module.member.ConsultingRequestLocationMappingProvider;
import com.motd.be.provider.module.member.ConsultingRequestProvider;
import com.motd.be.provider.module.member.ConsultingSheetFileProvider;
import com.motd.be.provider.module.member.ConsultingSheetProvider;
import com.motd.be.provider.module.member.DirectorInfoProvider;
import com.motd.be.provider.module.member.DirectorLocationMappingProvider;
import com.motd.be.provider.module.member.DirectorProfileDetailFileProvider;
import com.motd.be.provider.module.member.DirectorServiceMappingProvider;
import com.motd.be.provider.module.member.DirectorServiceProvider;
import com.motd.be.provider.module.member.FcmTokenProvider;
import com.motd.be.provider.module.member.ForbiddenWordProvider;
import com.motd.be.provider.module.member.LocationProvider;
import com.motd.be.provider.module.member.MemberBlockProvider;
import com.motd.be.provider.module.member.MemberDirectorFavoriteProvider;
import com.motd.be.provider.module.member.MemberLocationMappingProvider;
import com.motd.be.provider.module.member.MemberMetadataProvider;
import com.motd.be.provider.module.member.MemberNicknameHistoryProvider;
import com.motd.be.provider.module.member.MemberProvider;
import com.motd.be.provider.module.member.MemberReportProvider;
import com.motd.be.provider.module.member.NotificationProvider;
import com.motd.be.provider.module.member.PopularPortfolioProvider;
import com.motd.be.provider.module.member.PopupFileProvider;
import com.motd.be.provider.module.member.PopupProvider;
import com.motd.be.provider.module.member.PortfolioFileProvider;
import com.motd.be.provider.module.member.PortfolioProvider;
import com.motd.be.provider.module.member.ProfileFileProvider;
import com.motd.be.provider.module.member.PromotionCodeProvider;
import com.motd.be.provider.module.member.PromptMessageProvider;
import com.motd.be.provider.module.member.PromptRoomProvider;
import com.motd.be.provider.module.member.RefreshTokenProvider;
import com.motd.be.provider.module.member.ReportFileProvider;
import com.motd.be.provider.module.member.RequestLocationMappingProvider;
import com.motd.be.provider.module.member.ReviewFileProvider;
import com.motd.be.provider.module.member.ReviewProvider;
import com.motd.be.provider.module.member.ServiceEstimateFileProvider;
import com.motd.be.provider.module.member.ServiceEstimateProvider;
import com.motd.be.provider.module.member.ServiceEstimateTemplateProvider;
import com.motd.be.provider.module.member.ServiceRequestFileProvider;
import com.motd.be.provider.module.member.ServiceRequestProvider;
import com.motd.be.provider.module.member.ServiceRequestWishTimeProvider;
import com.motd.be.provider.module.member.StoryProvider;
import com.motd.be.provider.redis.RedisProvider;
import com.motd.be.provider.redis.domain.RedisAccessTokenProvider;
import com.motd.be.provider.redis.domain.RedisBlackListProvider;
import com.motd.be.provider.redis.domain.RedisChatRoomSubscribeProvider;
import com.motd.be.provider.redis.domain.RedisDirectorHideRequestProvider;
import com.motd.be.provider.redis.domain.RedisServiceRequestExpireProvider;
import com.motd.be.provider.redis.domain.RedisServiceRequestLocationExpandProvider;
import com.motd.be.provider.redis.domain.SignInBridgeCodeProvider;
import com.motd.be.provider.redis.domain.SignUpInformationProvider;
import com.motd.be.shared.ai.provider.AiChatProvider;
import com.motd.be.shared.aws.service.S3PresignedUrlService;
import com.motd.be.shared.aws.service.S3Uploader;
import com.motd.be.shared.hackle.service.HackleEventPublisher;
import com.motd.be.shared.mobile_ok.service.MobileOkCryptoService;
import com.motd.be.utils.AppleOauthTokenGenerator;

import jakarta.persistence.EntityManager;

public abstract class BaseIntegrationTest {

	@Autowired
	protected MockMvc mockMvc;
	@Autowired
	protected ObjectMapper objectMapper;
	@Autowired
	protected EntityManager entityManager;
	@Autowired
	protected RedisProvider redisProvider;
	@Autowired
	protected AppleOauthTokenGenerator appleOauthTokenGenerator;
	@Autowired
	protected MemberProvider memberProvider;
	@Autowired
	protected PopupProvider popUpProvider;
	@Autowired
	protected WireMockServer wireMockServer;
	@Autowired
	protected AppleRefreshTokenProvider appleTokenProvider;
	@Autowired
	protected RefreshTokenProvider refreshTokenProvider;
	@Autowired
	protected LocationProvider locationProvider;
	@Autowired
	protected DirectorInfoProvider directorInfoProvider;
	@Autowired
	protected DirectorLocationMappingProvider directorLocationMappingProvider;
	@Autowired
	protected DirectorServiceProvider directorCategoryProvider;
	@Autowired
	protected MemberMetadataProvider memberMetadataProvider;
	@Autowired
	protected PortfolioProvider portfolioProvider;
	@Autowired
	protected PortfolioFileProvider portfolioFileProvider;
	@Autowired
	protected DirectorServiceMappingProvider directorServiceMappingProvider;
	@Autowired
	protected DirectorServiceProvider directorServiceProvider;
	@Autowired
	protected ServiceRequestProvider serviceRequestProvider;
	@Autowired
	protected ServiceRequestWishTimeProvider serviceRequestWishTimeProvider;
	@Autowired
	protected ServiceRequestFileProvider serviceRequestFileProvider;
	@Autowired
	protected RequestLocationMappingProvider requestLocationMappingProvider;
	@Autowired
	protected ServiceEstimateProvider serviceEstimateProvider;
	@Autowired
	protected ChatRoomProvider chatRoomProvider;
	@Autowired
	protected ChatRoomMemberProvider chatRoomMemberProvider;
	@Autowired
	protected ChatMessageProvider chatMessageProvider;
	@Autowired
	protected ChatRoomServiceEstimateMappingProvider chatRoomServiceEstimateMappingProvider;
	@Autowired
	protected ServiceEstimateTemplateProvider serviceEstimateTemplateProvider;
	@Autowired
	protected ServiceEstimateFileProvider serviceEstimateFileProvider;
	@Autowired
	protected RedisChatRoomSubscribeProvider redisChatRoomSubscribeProvider;
	@Autowired
	protected ChatFileProvider chatFileProvider;
	@Autowired
	protected StoryProvider storyProvider;
	@Autowired
	protected ReviewProvider reviewProvider;
	@Autowired
	protected ReviewFileProvider reviewFileProvider;
	@Autowired
	protected ReportFileProvider reportFileProvider;
	@Autowired
	protected MemberBlockProvider memberBlockProvider;
	@Autowired
	protected MemberDirectorFavoriteProvider memberDirectorFavoriteProvider;
	@Autowired
	protected MemberReportProvider memberReportProvider;
	@Autowired
	protected NotificationProvider notificationProvider;
	@Autowired
	protected ReviewFileRepository reviewFileRepository;
	@Autowired
	protected ReportFileRepository reportFileRepository;
	@Autowired
	protected CashProductProvider cashProductProvider;
	@Autowired
	protected CashTransactionHistoryProvider cashTransactionHistoryProvider;
	@Autowired
	protected MemberLocationMappingProvider memberLocationMappingProvider;
	@Autowired
	protected BannerProvider bannerProvider;
	@Autowired
	protected DirectorProfileDetailFileProvider directorProfileDetailFileProvider;
	@Autowired
	protected MemberNicknameHistoryProvider memberNicknameHistoryProvider;
	@Autowired
	protected ProfileFileProvider profileFileProvider;
	@Autowired
	protected BusinessRegistrationFileProvider businessRegistrationFileProvider;
	@Autowired
	protected BusinessRegistrationProvider businessRegistrationProvider;
	@Autowired
	protected CodeUsageHistoryProvider codeUsageHistoryProvider;
	@Autowired
	protected ConsultingRequestProvider consultingRequestProvider;
	@Autowired
	protected ConsultingRequestFileProvider consultingRequestFileProvider;
	@Autowired
	protected ConsultingRequestLocationMappingProvider consultingRequestLocationMappingProvider;
	@Autowired
	protected ConsultingSheetProvider consultingSheetProvider;
	@Autowired
	protected ConsultingSheetFileProvider consultingSheetFileProvider;
	@Autowired
	protected PromotionCodeProvider promotionCodeProvider;
	@Autowired
	protected PopularPortfolioProvider popularPortfolioProvider;
	@Autowired
	protected StringEncryptor stringEncryptor;
	@Autowired
	protected FcmTokenProvider fcmTokenProvider;
	@Autowired
	protected BannerFileProvider bannerFileProvider;
	@Autowired
	protected PopupFileProvider popupFileProvider;
	@Autowired
	protected ForbiddenWordProvider forbiddenWordProvider;
	@Autowired
	protected PromptRoomProvider promptRoomProvider;
	@Autowired
	protected PromptMessageProvider promptMessageProvider;
	//mockito
	@MockitoBean
	protected EventPublisherAdapter eventPublisher;
	@MockitoBean
	protected GoogleIdTokenVerifier googleIdTokenVerifier;
	@MockitoBean
	protected S3PresignedUrlService s3PresignedUrlService;
	@MockitoBean
	protected S3Uploader s3Uploader;
	@MockitoBean
	private MobileOkCryptoService mobileOkCryptoService;
	@MockitoSpyBean
	protected HackleEventPublisher hackleEventPublisher;
	//redis
	@Autowired
	protected RedisAccessTokenProvider redisAccessTokenUtilProvider;
	@Autowired
	protected RedisServiceRequestLocationExpandProvider redisServiceRequestLocationExpandProvider;
	@Autowired
	protected RedisBlackListProvider redisBlackListUtilProvider;
	@Autowired
	protected SignUpInformationProvider signUpInformationProvider;
	@Autowired
	protected SignInBridgeCodeProvider signInBridgeCodeProvider;
	@Autowired
	protected RedisServiceRequestExpireProvider redisServiceRequestExpireProvider;
	@Autowired
	protected RedisDirectorHideRequestProvider redisDirectorHideRequestProvider;
	//admin
	@Autowired
	protected AdminProvider adminProvider;
	@Autowired
	protected PopupAdminProvider popupAdminProvider;
	//global
	@MockitoBean
	protected AiChatProvider aiChatProvider;
	@MockitoSpyBean
	protected Clock clock;
	@MockitoSpyBean
	protected SseService sseService;

	@AfterEach
	public void afterEach() {
		redisProvider.flushDB();
	}

}
