package com.motd.be.module.admin.admin_file.dto.request;

import static com.motd.be.common.constants.ValidationMessages.*;

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
public class AdminFileProcessStatusRequestForAdmin {

	@NotNull(message = FILE_ID_REQUIRED)
	private Long fileId;
	@NotBlank(message = DIRECTORY_REQUIRED)
	private String directoryType;
}
