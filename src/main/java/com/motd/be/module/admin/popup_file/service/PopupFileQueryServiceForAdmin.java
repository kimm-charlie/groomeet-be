package com.motd.be.module.admin.popup_file.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.PopupFileException;
import com.motd.be.module.admin.admin_file.entity.AdminFileForAdmin;
import com.motd.be.module.admin.popup_file.repository.PopupFileRepositoryForAdmin;
import com.motd.be.module.member.popup_file.entity.PopupFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PopupFileQueryServiceForAdmin {

	private final PopupFileRepositoryForAdmin popupFileRepositoryForAdmin;

	public PopupFile findById(Long fileId) {
		return popupFileRepositoryForAdmin.findById(fileId)
			.orElseThrow(() -> new CustomRuntimeException(PopupFileException.NOT_FOUND));
	}

	public List<PopupFile> findAllByIds(List<Long> fileIds) {
		return popupFileRepositoryForAdmin.findAllByIds(fileIds);
	}

	public AdminFileForAdmin findByFileKey(String fileKey) {
		return popupFileRepositoryForAdmin.findByFileKey(fileKey)
			.orElseThrow(() -> new CustomRuntimeException(PopupFileException.NOT_FOUND));
	}
}

