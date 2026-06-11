package com.motd.be.module.admin.banner.dto.request;

import static com.motd.be.common.constants.ValidationMessages.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class BannerUpdateRequestForAdmin {

	@Size(max = 100, message = BANNER_TITLE_MAX_LENGTH_MSG)
	private String title;
	@NotBlank(message = BANNER_START_AT_REQUIRED)
	private String startAt;
	@NotBlank(message = BANNER_END_AT_REQUIRED)
	private String endAt;
	@NotNull(message = BANNER_SORT_ORDER_REQUIRED)
	private Integer sortOrder;
	@NotNull(message = BANNER_IS_WEB_VIEW_REQUIRED)
	private Boolean isWebViewBanner;
	private String webViewUrl;
	@NotNull(message = BANNER_THUMBNAIL_FILE_ID_REQUIRED)
	private Long thumbnailFileId;
	private Long contentFileId;
}
