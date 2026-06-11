package com.motd.be.provider.module.member;

import static com.motd.be.Constants.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.notification.entity.Notification;
import com.motd.be.module.member.notification.entity.NotificationReceiverType;
import com.motd.be.module.member.notification.entity.NotificationType;
import com.motd.be.module.member.notification.repository.NotificationRepository;

@Component
public class NotificationProvider {

	@Autowired
	private NotificationRepository notificationRepository;

	public Notification save(Member receiver, NotificationType notificationType,
		Long referenceId, NotificationReceiverType receiverType) {
		return notificationRepository.save(Notification.builder()
			.receiver(receiver)
			.type(notificationType)
			.title(TITLE_STR)
			.content(CONTENT_STR)
			.referenceId(referenceId)
			.referenceType(notificationType.getReferenceType())
			.receiverType(receiverType)
			.build());
	}

	public Notification saveWithIsReadTrue(Member receiver, NotificationType notificationType,
		Long referenceId, NotificationReceiverType receiverType) {
		return notificationRepository.save(Notification.builder()
			.receiver(receiver)
			.type(notificationType)
			.title(TITLE_STR)
			.content(CONTENT_STR)
			.referenceId(referenceId)
			.referenceType(notificationType.getReferenceType())
			.receiverType(receiverType)
			.isRead(true)
			.build());
	}

	public List<Notification> findAll() {
		return notificationRepository.findAll();
	}
}
