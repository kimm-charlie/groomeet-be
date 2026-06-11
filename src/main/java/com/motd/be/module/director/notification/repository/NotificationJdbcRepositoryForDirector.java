package com.motd.be.module.director.notification.repository;

import static com.motd.be.common.constants.BatchConstant.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.motd.be.module.member.notification.entity.Notification;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationJdbcRepositoryForDirector {

	private final JdbcTemplate jdbcTemplate;

	public void batchInsert(List<Notification> notifications) {

		String sql = """
			INSERT INTO notification (
			    receiver_id,
			    type,
			    title,
			    content,
			    reference_id,
			    reference_type,
			    receiver_type,
			    is_read,
			    sender_id
			)
			VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
			""";

		for (int start = 0; start < notifications.size(); start += BATCH_PAGE_SIZE) {
			int end = Math.min(start + BATCH_PAGE_SIZE, notifications.size());
			List<Notification> batchList = notifications.subList(start, end);

			jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					Notification n = batchList.get(i);

					ps.setLong(1, n.getReceiver().getId());
					ps.setString(2, n.getType().name());
					ps.setString(3, n.getTitle());
					ps.setString(4, n.getContent());
					ps.setLong(5, n.getReferenceId());
					ps.setString(6, n.getReferenceType().name());
					ps.setString(7, n.getReceiverType().name());
					ps.setBoolean(8, Boolean.FALSE);
					ps.setObject(9, n.getSenderId(), java.sql.Types.BIGINT);
				}

				@Override
				public int getBatchSize() {
					return batchList.size();
				}
			});
		}
	}
}
