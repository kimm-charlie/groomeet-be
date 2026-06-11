package com.motd.be.module.admin.popup.dto.request;

import static com.motd.be.common.constants.ValidationMessages.*;
import static com.motd.be.common.utils.DateFormatUtils.*;

import com.motd.be.module.member.popup.entity.Popup;
import com.motd.be.module.member.popup.entity.PopupType;
import com.motd.be.module.member.popup_file.entity.PopupFile;

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
public class PopupSaveRequestForAdmin {

	@Size(max = 100, message = POPUP_TITLE_MAX_LENGTH_MSG)
	private String title;
	@NotBlank(message = POPUP_START_AT_REQUIRED)
	private String startAt;
	@NotBlank(message = POPUP_END_AT_REQUIRED)
	private String endAt;
	@NotNull(message = POPUP_SORT_ORDER_REQUIRED)
	private Integer sortOrder;
	@NotNull(message = POPUP_THUMBNAIL_FILE_ID_REQUIRED)
	private Long thumbnailFileId;
	private String linkUrl;
	@NotNull(message = POPUP_TYPE_REQUIRED)
	private PopupType type;

	public Popup toEntity(PopupFile thumbnailFile) {
		return Popup.builder()
			.title(this.title)
			.cdnThumbnailImageUrl(thumbnailFile.getCdnUrl())
			.linkUrl(this.linkUrl)
			.type(this.type)
			.startAt(parseToLocalDateTime(this.startAt))
			.endAt(parseToLocalDateTime(this.endAt))
			.sortOrder(this.sortOrder)
			.isDeleted(false)
			.thumbnailFile(thumbnailFile)
			.build();
	}
}


