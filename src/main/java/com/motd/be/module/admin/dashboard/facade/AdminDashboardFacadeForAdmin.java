package com.motd.be.module.admin.dashboard.facade;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.admin.dashboard.dto.response.AdminDashboardResponseForAdmin;
import com.motd.be.module.admin.dashboard.service.AdminDashboardServiceForAdmin;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardFacadeForAdmin {

	private final AdminDashboardServiceForAdmin adminDashboardServiceForAdmin;

	public AdminDashboardResponseForAdmin findDashboard() {
		return adminDashboardServiceForAdmin.findDashboard();
	}
}
