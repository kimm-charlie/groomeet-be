package com.motd.be.module.admin.dashboard.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.admin.dashboard.dto.response.AdminDashboardResponseForAdmin;
import com.motd.be.module.admin.dashboard.facade.AdminDashboardFacadeForAdmin;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminDashboardControllerForAdmin {

	private final AdminDashboardFacadeForAdmin adminDashboardFacadeForAdmin;

	@GetMapping("/dashboard")
	@PreAuthorize("hasAnyRole('ADMIN')")
	public ResponseEntity<AdminDashboardResponseForAdmin> findDashboard() {
		return ResponseEntity.status(HttpStatus.OK).body(adminDashboardFacadeForAdmin.findDashboard());
	}
}
