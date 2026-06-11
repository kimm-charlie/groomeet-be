package com.motd.be.provider.module.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.motd.be.module.admin.admin.entity.Admin;
import com.motd.be.module.admin.admin.repository.AdminRepository;

@Component
public class AdminProvider {

	@Autowired
	private AdminRepository adminRepository;
	@Autowired
	private BCryptPasswordEncoder encoder;

	public Admin save(String email, String password) {
		return adminRepository.save(adminDummy(email, password));
	}

	private Admin adminDummy(String email, String password) {
		return Admin.builder()
			.email(email)
			.nickname("admin")
			// encoding 해서 넣으면 너무 오래걸려서 테스트에서는 일반 문자열로 넣었다.
			.password(password)
			.build();
	}

	public Admin saveWithEncodedPassword(String email, String password) {
		return adminRepository.save(adminDummyWithEncodedPassword(email, password));
	}

	private Admin adminDummyWithEncodedPassword(String email, String password) {
		return Admin.builder()
			.email(email)
			.nickname("admin")
			.password(encoder.encode(password))
			.build();
	}
}
