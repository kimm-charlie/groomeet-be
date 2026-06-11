package com.motd.be.module.member.service_request.repository;

import static com.motd.be.common.constants.BatchConstant.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ServiceRequestJdbcRepository {

	private final JdbcTemplate jdbcTemplate;

	public void batchUpdateExpandedLocations(List<LocationExpansionUpdate> updates) {

		String sql = """
			UPDATE service_request
			   SET expanded_location_id = ?,
			       is_location_expanded = true,
			       location_expanded_at = ?
			 WHERE id = ?
			""";

		for (int start = 0; start < updates.size(); start += BATCH_PAGE_SIZE) {
			int end = Math.min(start + BATCH_PAGE_SIZE, updates.size());
			List<LocationExpansionUpdate> batchList = updates.subList(start, end);

			jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					LocationExpansionUpdate update = batchList.get(i);

					ps.setLong(1, update.expandedLocationId());
					ps.setTimestamp(2, Timestamp.valueOf(update.locationExpandedAt()));
					ps.setLong(3, update.serviceRequestId());
				}

				@Override
				public int getBatchSize() {
					return batchList.size();
				}
			});
		}
	}

	public record LocationExpansionUpdate(Long serviceRequestId, Long expandedLocationId,
										  LocalDateTime locationExpandedAt) {
	}
}
