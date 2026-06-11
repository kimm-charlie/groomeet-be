package com.motd.be.module.member.popup_file.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.motd.be.module.member.popup_file.entity.PopupFile;

public interface PopupFileRepository extends JpaRepository<PopupFile, Long> {
}
