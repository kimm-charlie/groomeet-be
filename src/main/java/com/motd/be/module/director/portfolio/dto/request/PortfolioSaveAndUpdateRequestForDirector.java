package com.motd.be.module.director.portfolio.dto.request;

import static com.motd.be.common.constants.ValidationConstants.*;
import static com.motd.be.common.constants.ValidationMessages.*;

import java.util.List;

import org.hibernate.validator.constraints.Length;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.portfolio.entity.Portfolio;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
public class PortfolioSaveAndUpdateRequestForDirector {

	@NotNull(message = DIRECTOR_SERVICE_ID_REQUIRED)
	private Long directorServiceId;
	@NotBlank(message = TITLE_REQUIRED)
	@Length(max = PORTFOLIO_MAX_TITLE_LENGTH, message = PORTFOLIO_TITLE_MAX_LENGTH_MSG)
	private String title;
	@NotEmpty(message = IMAGE_IDS_REQUIRED)
	@Size(max = PORTFOLIO_MAX_IMAGE_COUNT, message = PORTFOLIO_IMAGE_MAX_COUNT_MSG)
	private List<Long> fileIds;
	@NotNull(message = PRICE_REQUIRED)
	@Max(value = PORTFOLIO_MAX_PRICE, message = PORTFOLIO_MAX_PRICE_MSG)
	private Long price;
	@NotBlank(message = CONTENT_REQUIRED)
	@Length(max = PORTFOLIO_MAX_CONTENT_LENGTH, message = PORTFOLIO_MAX_CONTENT_LENGTH_MSG)
	private String content;
	@NotNull(message = THUMBNAIL_IMAGE_ID_REQUIRED)
	private Long thumbnailImageId;

	public Portfolio toEntity(DirectorInfo directorInfo, DirectorService directorService) {
		return Portfolio.builder()
			.directorInfo(directorInfo)
			.directorService(directorService)
			.title(this.title)
			.price(this.price)
			.content(this.content)
			.build();
	}
}

