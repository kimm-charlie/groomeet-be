package com.motd.be.module.admin.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.notification.entity.Notification;
import com.motd.be.module.member.notification.entity.NotificationReceiverType;

public interface NotificationRepositoryForAdmin extends JpaRepository<Notification, Long> {

	@Query("""
			SELECT COUNT(n)
			FROM Notification n
			WHERE n.receiver = :receiver
			AND n.receiverType = :notificationReceiverType
			AND n.isRead = false
			AND n.isDeleted = false
		""")
	int countUnreadByReceiverIdAndReceiverType(@Param("receiver") Member receiver,
		@Param("notificationReceiverType") NotificationReceiverType notificationReceiverType);
}
