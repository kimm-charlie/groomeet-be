package com.motd.be.provider.module.member;

import static com.motd.be.Constants.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.banner.entity.Banner;
import com.motd.be.module.member.banner.entity.BannerType;
import com.motd.be.module.member.banner.repository.BannerRepository;
import com.motd.be.module.member.banner_file.entity.BannerFile;

@Component
public class BannerProvider {

	@Autowired
	private BannerRepository bannerRepository;

	public Banner save(LocalDateTime startAt, LocalDateTime endAt, int sortOrder) {
		return bannerRepository.save(Banner.builder()
			.title(TITLE_STR)
			.contentImageCdnUrl(CONTENT_IMAGE_URL)
			.thumbnailImageCdnUrl(THUMBNAIL_IMAGE_URL)
			.startAt(startAt)
			.endAt(endAt)
			.sortOrder(sortOrder)
			.isWebViewBanner(Boolean.FALSE)
			.type(BannerType.MEMBER)
			.build());
	}

	public Banner saveWithTitle(String title, LocalDateTime startAt, LocalDateTime endAt, int sortOrder) {
		return bannerRepository.save(Banner.builder()
			.title(title)
			.contentImageCdnUrl(CONTENT_IMAGE_URL)
			.thumbnailImageCdnUrl(THUMBNAIL_IMAGE_URL)
			.startAt(startAt)
			.endAt(endAt)
			.sortOrder(sortOrder)
			.isWebViewBanner(Boolean.FALSE)
			.type(BannerType.MEMBER)
			.build());
	}

	public Banner saveWithFiles(LocalDateTime startAt, LocalDateTime endAt, int sortOrder,
		BannerFile thumbnailFile, BannerFile contentFile) {
		return bannerRepository.save(Banner.builder()
			.title(TITLE_STR)
			.contentImageCdnUrl(contentFile != null ? contentFile.getCdnUrl() : null)
			.contentFile(contentFile)
			.thumbnailImageCdnUrl(thumbnailFile.getCdnUrl())
			.thumbnailFile(thumbnailFile)
			.startAt(startAt)
			.endAt(endAt)
			.sortOrder(sortOrder)
			.isWebViewBanner(Boolean.FALSE)
			.type(BannerType.MEMBER)
			.build());
	}

	public Banner saveWithIsDeletedTrue(LocalDateTime startAt, LocalDateTime endAt, int sortOrder) {
		return bannerRepository.save(Banner.builder()
			.title(TITLE_STR)
			.contentImageCdnUrl(CONTENT_IMAGE_URL)
			.thumbnailImageCdnUrl(THUMBNAIL_IMAGE_URL)
			.startAt(startAt)
			.endAt(endAt)
			.sortOrder(sortOrder)
			.isWebViewBanner(Boolean.FALSE)
			.isDeleted(Boolean.TRUE)
			.type(BannerType.MEMBER)
			.build());
	}

	public Banner saveWithType(BannerType type, LocalDateTime startAt, LocalDateTime endAt, int sortOrder) {
		return bannerRepository.save(Banner.builder()
			.title(TITLE_STR)
			.contentImageCdnUrl(CONTENT_IMAGE_URL)
			.thumbnailImageCdnUrl(THUMBNAIL_IMAGE_URL)
			.startAt(startAt)
			.endAt(endAt)
			.sortOrder(sortOrder)
			.isWebViewBanner(Boolean.FALSE)
			.type(type)
			.build());
	}

	public Banner saveWithTypeAndIsDeletedTrue(BannerType type, LocalDateTime startAt, LocalDateTime endAt,
		int sortOrder) {
		return bannerRepository.save(Banner.builder()
			.title(TITLE_STR)
			.contentImageCdnUrl(CONTENT_IMAGE_URL)
			.thumbnailImageCdnUrl(THUMBNAIL_IMAGE_URL)
			.startAt(startAt)
			.endAt(endAt)
			.sortOrder(sortOrder)
			.isWebViewBanner(Boolean.FALSE)
			.isDeleted(Boolean.TRUE)
			.type(type)
			.build());
	}

	public List<Banner> findAll() {
		return bannerRepository.findAll();
	}

	public Banner findById(Long id) {
		return bannerRepository.findById(id).orElseThrow();
	}
}
