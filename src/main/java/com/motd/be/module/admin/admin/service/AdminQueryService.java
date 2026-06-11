package com.motd.be.module.admin.admin.service;

import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.AdminException;
import com.motd.be.module.admin.admin.entity.Admin;
import com.motd.be.module.admin.admin.repository.AdminRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminQueryService {

	private final AdminRepository adminRepository;

	public Admin findByEmail(String email) {
		return adminRepository.findByEmail(email)
			.orElseThrow(() -> new CustomRuntimeException(AdminException.NOT_FOUND));
	}

	public Admin findById(Long adminId) {
		return adminRepository.findById(adminId)
			.orElseThrow(() -> new CustomRuntimeException(AdminException.NOT_FOUND));
	}
}
