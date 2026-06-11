package com.motd.be.module.admin.admin_file.dto.request;

import com.motd.be.module.member.file.enums.FileProcessStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class AdminFileUpdateProcessRequestForAdmin {

	@NotBlank
	private String fileKey;
	@NotNull
	private FileProcessStatus processStatus;
}
