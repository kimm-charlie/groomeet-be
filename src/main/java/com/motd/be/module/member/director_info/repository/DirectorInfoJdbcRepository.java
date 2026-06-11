package com.motd.be.module.member.director_info.repository;

import static com.motd.be.common.constants.BatchConstant.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DirectorInfoJdbcRepository {

	private final JdbcTemplate jdbcTemplate;

	public void updateCompletedEstimateCountsByMembers(Map<Long, Integer> directorEstimateCountMap) {

		String sql = """
			
			UPDATE director_info
			     SET completed_estimate_count = completed_estimate_count + ?
			     WHERE id = ?
			""";

		List<Map.Entry<Long, Integer>> entries = new ArrayList<>(directorEstimateCountMap.entrySet());

		for (int start = 0; start < entries.size(); start += BATCH_PAGE_SIZE) {
			int end = Math.min(start + BATCH_PAGE_SIZE, entries.size());
			List<Map.Entry<Long, Integer>> batchList = entries.subList(start, end);

			jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					Map.Entry<Long, Integer> e = batchList.get(i);

					Long directorInfoId = e.getKey();
					Integer increment = e.getValue();

					ps.setInt(1, increment);
					ps.setLong(2, directorInfoId);
				}

				@Override
				public int getBatchSize() {
					return batchList.size();
				}
			});
		}
	}
}
