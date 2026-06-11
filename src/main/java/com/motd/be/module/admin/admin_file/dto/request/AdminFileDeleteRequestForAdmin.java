package com.motd.be.module.admin.admin_file.dto.request;

import static com.motd.be.common.constants.ValidationMessages.*;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class AdminFileDeleteRequestForAdmin {

	@NotEmpty(message = FILE_ID_REQUIRED)
	private List<Long> fileIds;
	@NotBlank(message = DIRECTORY_REQUIRED)
	private String directoryType;
}
