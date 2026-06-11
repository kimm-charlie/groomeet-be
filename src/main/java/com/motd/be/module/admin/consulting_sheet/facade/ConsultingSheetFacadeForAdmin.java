package com.motd.be.module.admin.consulting_sheet.facade;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.admin.banner.service.BannerQueryServiceForAdmin;
import com.motd.be.module.admin.consulting_sheet.service.ConsultingSheetQueryServiceForAdmin;
import com.motd.be.module.admin.consulting_sheet.service.ConsultingSheetServiceForAdmin;
import com.motd.be.module.admin.notification.service.NotificationServiceForAdmin;
import com.motd.be.module.member.banner.entity.Banner;
import com.motd.be.module.member.consulting_sheet.entity.ConsultingSheet;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.shared.firebase.dto.FirebasePushEvent;
import com.motd.be.shared.firebase.service.FirebaseEventPublisher;
import com.motd.be.shared.firebase.service.FirebasePushFactory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultingSheetFacadeForAdmin {

	private final ConsultingSheetServiceForAdmin consultingSheetServiceForAdmin;
	private final ConsultingSheetQueryServiceForAdmin consultingSheetQueryServiceForAdmin;
	private final NotificationServiceForAdmin notificationServiceForAdmin;
	private final BannerQueryServiceForAdmin bannerQueryServiceForAdmin;
	private final FirebasePushFactory firebasePushFactory;
	private final FirebaseEventPublisher firebaseEventPublisher;

	@Transactional
	public void approve(Long consultingSheetId) {
		ConsultingSheet consultingSheet = consultingSheetQueryServiceForAdmin.findByIdWithLock(consultingSheetId);
		consultingSheetServiceForAdmin.approve(consultingSheet);

		Member receiver = consultingSheet.getConsultingRequest().getMember();
		Member directorMember = consultingSheet.getDirectorInfo().getMember();

		Optional<Banner> banner = bannerQueryServiceForAdmin.findActiveMemberBannerByTitleContaining("컨설팅 요청");
		Long bannerId = banner.map(Banner::getId).orElse(null);

		// Notification 저장 + SSE refresh event 발행
		notificationServiceForAdmin.saveConsultingSheetApprovedNotification(directorMember, receiver, bannerId);

		// 유효한 배너가 있으면 Firebase push event 발행
		banner.ifPresent(b -> {
			FirebasePushEvent event = firebasePushFactory.consultingSheetApprovedToMember(
				directorMember, receiver, b.getId());
			firebaseEventPublisher.sendPush(event);
		});
	}

	@Transactional
	public void reject(Long consultingSheetId) {
		ConsultingSheet consultingSheet = consultingSheetQueryServiceForAdmin.findByIdWithLock(consultingSheetId);
		consultingSheetServiceForAdmin.reject(consultingSheet);
	}
}
