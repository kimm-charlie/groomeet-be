package com.motd.be.module.director.cash.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.motd.be.module.member.cash.entity.CashProduct;

public interface CashProductRepositoryForDirector extends JpaRepository<CashProduct, Long> {

	@Query("""
			SELECT cp
			FROM CashProduct cp
			WHERE cp.isDeleted = false
			ORDER BY cp.price ASC
		""")
	List<CashProduct> findAllAvailable();
}
