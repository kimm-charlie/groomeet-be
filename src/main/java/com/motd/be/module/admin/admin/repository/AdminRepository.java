package com.motd.be.module.admin.admin.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.web.bind.annotation.RequestParam;

import com.motd.be.module.admin.admin.entity.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {

	@Query("""
			SELECT a 
			FROM Admin a 
			WHERE a.email = :email
		""")
	Optional<Admin> findByEmail(@RequestParam("email") String email);
}
