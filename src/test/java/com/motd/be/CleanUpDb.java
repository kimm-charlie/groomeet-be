package com.motd.be;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.motd.be.provider.redis.RedisProvider;

import jakarta.persistence.EntityManager;

@Component
public class CleanUpDb {

	@Autowired
	private EntityManager entityManager;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private RedisProvider redisProvider;

	public void all() {
		// 1. Hibernate SessionFactory에서 실제 테이블명 가져오기
		var sessionFactory = entityManager.getEntityManagerFactory()
			.unwrap(org.hibernate.engine.spi.SessionFactoryImplementor.class);

		var tableNames = sessionFactory.getMetamodel()
			.entityPersisters()
			.values()
			.stream()
			.map(p -> ((org.hibernate.persister.entity.AbstractEntityPersister)p).getTableName())
			.distinct()
			.toList();

		// 2. 순회하면서 DELETE FROM 실행
		for (String tableName : tableNames) {
			try {
				jdbcTemplate.execute("DELETE FROM " + tableName);
			} catch (Exception e) {
				System.out.println("삭제 실패: " + tableName + " (" + e.getMessage() + ")");
			}
		}

		// 3. Redis 데이터도 같이 정리
		redisProvider.flushDB();
	}

}
