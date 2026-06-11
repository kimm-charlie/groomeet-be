package com.motd.be.module.director.service_estimate_file.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ServiceEstimateFileException;
import com.motd.be.module.director.director_service_mapping.service.result.DirectorServiceMappingUpdateResult;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate_file.entity.ServiceEstimateFile;
import com.motd.be.module.member.service_estimate_file.entity.ServiceEstimateType;
import com.motd.be.module.member.service_estimate_file.validator.ServiceEstimateFileValidator;
import com.motd.be.module.member.service_estimate_template.entity.ServiceEstimateTemplate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceEstimateFileServiceForDirector {

	private final ServiceEstimateFileQueryServiceForDirector serviceEstimateTemplateFileQueryService;
	private final ServiceEstimateFileValidator serviceEstimateFileValidator;
	private final ServiceEstimateFileCommandServiceForDirector serviceEstimateFileCommandserviceForDirector;

	public List<ServiceEstimateFile> findAllByIdsWithValidation(List<Long> requestedFileIds, Member member) {
		if (requestedFileIds == null || requestedFileIds.isEmpty())
			return List.of();

		// 제안서 이미지 조회
		List<ServiceEstimateFile> images = serviceEstimateTemplateFileQueryService.findAllByIds(
			requestedFileIds);

		// 제안서 이미지 검증
		serviceEstimateFileValidator.validateEstimateImages(member, images, requestedFileIds);

		return images;
	}

	public void mapServiceTemplate(ServiceEstimateTemplate serviceEstimateTemplate,
		List<ServiceEstimateFile> filesFromDb, List<Long> fileIdsFromRequest) {

		if (filesFromDb == null || filesFromDb.isEmpty())
			return;

		// 순서 정렬
		Map<Long, Integer> sortOrderMap =
			IntStream.range(0, fileIdsFromRequest.size())
				.boxed()
				.collect(Collectors.toMap(
					fileIdsFromRequest::get,  // key: id
					i -> i        // value: index
				));

		serviceEstimateFileCommandserviceForDirector.updateSortOrder(sortOrderMap);

		serviceEstimateFileCommandserviceForDirector.updateServiceEstimateTemplate(serviceEstimateTemplate,
			filesFromDb);
	}

	public void updateImagesAndMapToTemplate(ServiceEstimateTemplate serviceEstimateTemplate,
		List<ServiceEstimateFile> filesFromDb, List<Long> fileIdsFromRequest) {

		// 기존 이미지 목록
		List<ServiceEstimateFile> existingImages = serviceEstimateTemplate.getImages();

		// 기존 이미지 ID와 새 이미지 ID를 세트로 변환
		Set<Long> existingIds = existingImages.stream()
			.map(ServiceEstimateFile::getId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		Set<Long> newIds = filesFromDb.stream()
			.map(ServiceEstimateFile::getId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		// 유지 (공통 ID)
		List<ServiceEstimateFile> toKeep = existingImages.stream()
			.filter(img -> newIds.contains(img.getId()))
			.toList();

		// 추가 (새로 들어온 이미지 중 기존에 없는 것)
		List<ServiceEstimateFile> toAdd = filesFromDb.stream()
			.filter(img -> !existingIds.contains(img.getId()))
			.toList();

		// 삭제 (기존 이미지 중 새 리스트에 없는 것)
		List<ServiceEstimateFile> toDelete = existingImages.stream()
			.filter(img -> !newIds.contains(img.getId()))
			.toList();

		// 삭제된 이미지 soft delete
		serviceEstimateFileCommandserviceForDirector.softDeleteAll(toDelete);

		// 순서 정렬
		if (fileIdsFromRequest == null || fileIdsFromRequest.isEmpty())
			return;

		Map<Long, Integer> sortOrderMap =
			IntStream.range(0, fileIdsFromRequest.size())
				.boxed()
				.collect(Collectors.toMap(
					fileIdsFromRequest::get,  // key: id
					i -> i        // value: index
				));

		serviceEstimateFileCommandserviceForDirector.updateSortOrder(sortOrderMap);

		// 새로 추가된 이미지 → 템플릿 매핑
		serviceEstimateFileCommandserviceForDirector.updateServiceEstimateTemplate(serviceEstimateTemplate,
			toAdd);
	}

	public List<ServiceEstimateFile> mapServiceEstimate(ServiceEstimate serviceEstimate,
		List<ServiceEstimateFile> imagesFromDb,
		List<Long> fileIdsFromRequest, Member member) {

		if (imagesFromDb == null || imagesFromDb.isEmpty())
			return List.of();

		// TEMPLATE 타입 이미지 선별
		List<ServiceEstimateFile> templateTypeImages = imagesFromDb.stream()
			.filter(img -> img.getEstimateType() == ServiceEstimateType.TEMPLATE)
			.toList();

		// TEMPLATE → ESTIMATE 로 복제 저장
		List<ServiceEstimateFile> newlySavedEstimateImages = templateTypeImages.stream()
			.map(templateImg -> ServiceEstimateFile.ofWithServiceEstimate(serviceEstimate, member, templateImg))
			.map(serviceEstimateFileCommandserviceForDirector::save)
			.toList();

		// TEMPLATE id → 새로 저장된 ESTIMATE id 매핑
		Map<Long, Long> idMapping = IntStream.range(0, templateTypeImages.size())
			.boxed()
			.collect(Collectors.toMap(
				i -> templateTypeImages.get(i).getId(),               // old id
				i -> newlySavedEstimateImages.get(i).getId()          // new id
			));

		// 기존 imagesFromDb 내 TEMPLATE 타입 이미지 → 새로 저장된 ESTIMATE 이미지로 교체
		List<ServiceEstimateFile> replacedFiles = imagesFromDb.stream()
			.map(img -> {
				if (img.getEstimateType() == ServiceEstimateType.TEMPLATE) {
					// 복제본 중 동일한 URL 매칭
					return newlySavedEstimateImages.stream()
						.filter(newImg -> newImg.getOriginUrl().equals(img.getOriginUrl()))
						.findFirst()
						.orElseThrow(() -> new CustomRuntimeException(
							ServiceEstimateFileException.INVALID_TEMPLATE_IMAGE_EXIST));
				}
				return img;
			}).toList();

		// 순서 정렬
		Map<Long, Integer> sortOrderMap =
			IntStream.range(0, fileIdsFromRequest.size())
				.boxed()
				.collect(Collectors.toMap(
					i -> {
						Long originalId = fileIdsFromRequest.get(i);
						// TEMPLATE → ESTIMATE 매핑 반영
						return idMapping.getOrDefault(originalId, originalId);
					},
					i -> i
				));

		serviceEstimateFileCommandserviceForDirector.updateSortOrder(sortOrderMap);
		serviceEstimateFileCommandserviceForDirector.updateServiceEstimate(serviceEstimate, replacedFiles);

		serviceEstimate.setFile(replacedFiles);

		return replacedFiles;
	}

	public void handleWhenDirectorServiceUpdated(DirectorInfo directorInfo,
		DirectorServiceMappingUpdateResult result) {
		// 1. 삭제된 템플릿 이미지 삭제
		serviceEstimateFileCommandserviceForDirector.deleteAllByDirectorInfoAndServiceId(
			directorInfo, result.getDeleted());

		// 2. 복구된 템플릿 이미지 복구
		serviceEstimateFileCommandserviceForDirector.restoreAllByDirectorInfoAndServiceId(
			directorInfo, result.getRestored());
	}

	public void deleteAllByServiceEstimateTemplateId(Long templateId) {
		serviceEstimateFileCommandserviceForDirector.deleteAllByServiceEstimateTemplateId(templateId);
	}
}
