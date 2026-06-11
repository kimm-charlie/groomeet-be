package com.motd.be.module.director.portfolio_file.repository;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PortfolioFileJdbcTemplateRepositoryForDirector {

	private final JdbcTemplate jdbcTemplate;

	public void updateSortOrder(Map<Long, Integer> sortOrderMap) {
		if (sortOrderMap == null || sortOrderMap.isEmpty())
			return;

		// CASE WHEN 구문 생성
		StringBuilder caseSql = new StringBuilder();
		for (Map.Entry<Long, Integer> entry : sortOrderMap.entrySet()) {
			caseSql.append("WHEN id = ")
				.append(entry.getKey())
				.append(" THEN ")
				.append(entry.getValue())
				.append(" ");
		}

		// id 리스트 문자열로 변환
		String idList = sortOrderMap.keySet().stream()
			.map(String::valueOf)
			.collect(Collectors.joining(","));

		// SQL
		String sql = String.format("""
			UPDATE portfolio_file
			SET sort_order = CASE %s END
			WHERE id IN (%s)
			""", caseSql, idList);

		jdbcTemplate.update(sql);
	}
}
