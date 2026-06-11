package com.motd.be.module.member.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.notification.entity.Notification;
import com.motd.be.module.member.notification.entity.NotificationReceiverType;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	@Modifying
	@Query("""
			UPDATE Notification n
			SET n.isRead = true
			WHERE n IN :notifications
		""")
	void markAllAsRead(@Param("notifications") List<Notification> notifications);

	@Query("""
		        SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END
		        FROM Notification n
		        WHERE n.receiver.id = :receiverId
		        AND n.receiverType = :receiverType
		        AND n.isRead = false
				AND n.isDeleted = false
		""")
	boolean existsUnreadByReceiverId(@Param("receiverId") Long receiverId,
		@Param("receiverType") NotificationReceiverType receiverType);

	@Modifying
	@Query("""
			UPDATE Notification n
			SET n.isDeleted = true
			WHERE n.receiver = :receiver
		""")
	void deleteAllByReceiver(@Param("receiver") Member receiver);

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
