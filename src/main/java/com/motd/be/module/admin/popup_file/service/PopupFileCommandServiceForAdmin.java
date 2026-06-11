package com.motd.be.module.admin.popup_file.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.admin.admin_file.entity.AdminFileForAdmin;
import com.motd.be.module.admin.popup_file.repository.PopupFileRepositoryForAdmin;
import com.motd.be.module.member.popup_file.entity.PopupFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PopupFileCommandServiceForAdmin {

	private final PopupFileRepositoryForAdmin popupFileRepositoryForAdmin;

	public AdminFileForAdmin save(PopupFile entity) {
		return popupFileRepositoryForAdmin.save(entity);
	}
}
