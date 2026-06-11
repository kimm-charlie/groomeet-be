package com.motd.be.module.member.file.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.FileException;
import com.motd.be.exception.exceptions.ImageException;
import com.motd.be.module.member.business_registration_file.entity.BusinessRegistrationFile;
import com.motd.be.module.member.business_registration_file.service.BusinessRegistrationFileCommandService;
import com.motd.be.module.member.business_registration_file.service.BusinessRegistrationFileQueryService;
import com.motd.be.module.member.chat_file.entity.ChatFile;
import com.motd.be.module.member.chat_file.service.ChatFileCommandService;
import com.motd.be.module.member.chat_file.service.ChatFileQueryService;
import com.motd.be.module.member.director_profile_detail_file.entity.DirectorProfileDetailFile;
import com.motd.be.module.member.director_profile_detail_file.service.DirectorProfileDetailFileCommandService;
import com.motd.be.module.member.director_profile_detail_file.service.DirectorProfileDetailFileQueryService;
import com.motd.be.module.member.file.dto.request.FileUpdateProcessRequest;
import com.motd.be.module.member.file.entity.BaseFile;
import com.motd.be.module.member.file.enums.FileProcessStatus;
import com.motd.be.module.member.file.validator.FileValidator;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.portfolio_file.entity.PortfolioFile;
import com.motd.be.module.member.portfolio_file.service.PortfolioFileCommandService;
import com.motd.be.module.member.portfolio_file.service.PortfolioFileQueryService;
import com.motd.be.module.member.profile_file.entity.ProfileFile;
import com.motd.be.module.member.profile_file.service.ProfileFileCommandService;
import com.motd.be.module.member.profile_file.service.ProfileFileQueryService;
import com.motd.be.module.member.report_file.entity.ReportFile;
import com.motd.be.module.member.report_file.service.ReportFileCommandService;
import com.motd.be.module.member.review_file.entity.ReviewFile;
import com.motd.be.module.member.review_file.service.ReviewFileCommandService;
import com.motd.be.module.member.service_estimate_file.entity.ServiceEstimateFile;
import com.motd.be.module.member.service_estimate_file.service.ServiceEstimateFileCommandService;
import com.motd.be.module.member.service_estimate_file.service.ServiceEstimateFileQueryService;
import com.motd.be.module.member.service_request_file.entity.ServiceRequestFile;
import com.motd.be.module.member.service_request_file.service.ServiceRequestFileCommandService;
import com.motd.be.module.member.service_request_file.service.ServiceRequestFileQueryService;
import com.motd.be.module.member.consulting_request_file.entity.ConsultingRequestFile;
import com.motd.be.module.member.consulting_request_file.service.ConsultingRequestFileCommandService;
import com.motd.be.module.member.consulting_request_file.service.ConsultingRequestFileQueryService;
import com.motd.be.module.member.consulting_sheet_file.entity.ConsultingSheetFile;
import com.motd.be.module.member.consulting_sheet_file.service.ConsultingSheetFileCommandService;
import com.motd.be.module.member.consulting_sheet_file.service.ConsultingSheetFileQueryService;
import com.motd.be.shared.aws.dto.GeneratedPresignedUrl;
import com.motd.be.shared.aws.enums.S3DirectoryType;
import com.motd.be.shared.aws.enums.UploadFileType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FileService {

	private final PortfolioFileCommandService portfolioFileCommandService;
	private final PortfolioFileQueryService portfolioFileQueryService;
	private final ChatFileCommandService chatFileCommandService;
	private final ChatFileQueryService chatFileQueryService;
	private final ServiceEstimateFileCommandService serviceEstimateFileCommandService;
	private final ServiceEstimateFileQueryService serviceEstimateFileQueryService;
	private final ReviewFileCommandService reviewFileCommandService;
	private final ServiceRequestFileCommandService serviceRequestFileCommandService;
	private final ServiceRequestFileQueryService serviceRequestFileQueryService;
	private final ReportFileCommandService reportFileCommandService;
	private final DirectorProfileDetailFileCommandService directorProfileDetailFileCommandService;
	private final DirectorProfileDetailFileQueryService directorProfileDetailFileQueryService;
	private final ProfileFileCommandService profileFileCommandService;
	private final ProfileFileQueryService profileFileQueryService;
	private final BusinessRegistrationFileCommandService businessRegistrationFileCommandService;
	private final BusinessRegistrationFileQueryService businessRegistrationFileQueryService;
	private final ConsultingRequestFileCommandService consultingRequestFileCommandService;
	private final ConsultingRequestFileQueryService consultingRequestFileQueryService;
	private final ConsultingSheetFileCommandService consultingSheetFileCommandService;
	private final ConsultingSheetFileQueryService consultingSheetFileQueryService;
	private final FileValidator fileValidator;

	/**
	 * 디렉터리 타입에 맞는 저장 처리
	 */
	public BaseFile saveImageByType(S3DirectoryType type, GeneratedPresignedUrl url, Member member,
		UploadFileType fileType, String fileName, String fileSize) {
		return switch (type) {
			case PROFILE -> profileFileCommandService.save(
				ProfileFile.of(url.getOriginUrl(), url.getFileKey(), member, fileType, fileName, fileSize));
			case PORTFOLIO -> portfolioFileCommandService.save(
				PortfolioFile.ofWithoutPortfolio(url.getOriginUrl(), url.getFileKey(), member, fileType, fileName,
					fileSize));
			case CHAT -> chatFileCommandService.save(
				ChatFile.ofWithoutChatMessage(url.getOriginUrl(), url.getFileKey(), member, fileType, fileName,
					fileSize));
			case SERVICE_ESTIMATE -> serviceEstimateFileCommandService.save(
				ServiceEstimateFile.ofWithoutServiceEstimate(url.getOriginUrl(), url.getFileKey(), member, fileType,
					fileName, fileSize));
			case REVIEW -> reviewFileCommandService.save(
				ReviewFile.ofWithoutReview(url.getOriginUrl(), url.getFileKey(), member, fileType, fileName, fileSize));
			case SERVICE_REQUEST -> serviceRequestFileCommandService.save(
				ServiceRequestFile.ofWithoutServiceRequest(url.getOriginUrl(), url.getFileKey(), member, fileType,
					fileName, fileSize));
			case REPORT -> reportFileCommandService.save(
				ReportFile.ofWithoutReport(url.getOriginUrl(), url.getFileKey(), member, fileType, fileName, fileSize));
			case DIRECTOR_PROFILE_DETAIL -> directorProfileDetailFileCommandService.save(
				DirectorProfileDetailFile.ofWithoutDirectorProfileDetail(url.getOriginUrl(), url.getFileKey(), member,
					fileType, fileName, fileSize));
			case BUSINESS_REGISTRATION -> businessRegistrationFileCommandService.save(
				BusinessRegistrationFile.ofWithoutBusinessRegistration(url.getOriginUrl(), url.getFileKey(), member,
					fileType, fileName, fileSize));
			case CONSULTING_REQUEST -> consultingRequestFileCommandService.save(
				ConsultingRequestFile.ofWithoutConsultingRequest(url.getOriginUrl(), url.getFileKey(), member, fileType,
					fileName, fileSize));
			case CONSULTING_SHEET -> consultingSheetFileCommandService.save(
				ConsultingSheetFile.ofWithoutConsultingSheet(url.getOriginUrl(), url.getFileKey(), member, fileType,
					fileName, fileSize));
			default -> throw new CustomRuntimeException(ImageException.UNSUPPORTED_DIRECTORY_TYPE);
		};
	}

	/**
	 * 이미지 삭제 (논리 삭제 + 권한 검증)
	 */
	@Transactional
	public void delete(List<? extends BaseFile> images, Long memberId) {

		images.forEach(image -> {
			if (!image.isOwnedBy(memberId)) {
				throw new CustomRuntimeException(ImageException.NOT_OWNED_BY);
			}
			image.delete();
		});
	}

	public List<? extends BaseFile> findImagesByType(S3DirectoryType type, List<Long> ids) {
		return switch (type) {
			case PROFILE -> profileFileQueryService.findAllByIds(ids);
			case PORTFOLIO -> portfolioFileQueryService.findAllByIds(ids);
			case CHAT -> chatFileQueryService.findAllByIds(ids);
			case SERVICE_ESTIMATE -> serviceEstimateFileQueryService.findAllByIds(ids);
			case REVIEW -> reviewFileCommandService.findAllByIds(ids);
			case SERVICE_REQUEST -> serviceRequestFileQueryService.findAllByIds(ids);
			case REPORT -> reportFileCommandService.findAllByIds(ids);
			case DIRECTOR_PROFILE_DETAIL -> directorProfileDetailFileQueryService.findAllByIds(ids);
			case BUSINESS_REGISTRATION -> businessRegistrationFileQueryService.findAllByIds(ids);
			case CONSULTING_REQUEST -> consultingRequestFileQueryService.findAllByIds(ids);
			case CONSULTING_SHEET -> consultingSheetFileQueryService.findAllByIds(ids);
			default -> throw new CustomRuntimeException(ImageException.UNSUPPORTED_DIRECTORY_TYPE);
		};
	}

	public void updateProcessStatus(String fileKey, FileUpdateProcessRequest request) {
		S3DirectoryType directoryType = S3DirectoryType.findByFileKey(fileKey);

		BaseFile file = findImageByFileKey(directoryType, fileKey);

		file.updateProcessStatus(request.getProcessStatus());
	}

	public FileProcessStatus getProcessStatus(S3DirectoryType directoryType, Long fileId, Long memberId) {
		List<? extends BaseFile> files = findImagesByType(directoryType, List.of(fileId));

		if (files.isEmpty()) {
			throw new CustomRuntimeException(FileException.FILE_NOT_FOUND);
		}
		BaseFile baseFile = files.get(0);

		fileValidator.validateOwnership(baseFile, memberId);

		return baseFile.getProcessStatus();
	}

	private BaseFile findImageByFileKey(S3DirectoryType type, String fileKey) {
		return switch (type) {
			case PROFILE -> profileFileQueryService.findByFileKey(fileKey);
			case PORTFOLIO -> portfolioFileQueryService.findByFileKey(fileKey);
			case CHAT -> chatFileQueryService.findByFileKey(fileKey);
			case SERVICE_ESTIMATE -> serviceEstimateFileQueryService.findByFileKey(fileKey);
			case REVIEW -> reviewFileCommandService.findByFileKey(fileKey);
			case SERVICE_REQUEST -> serviceRequestFileQueryService.findByFileKey(fileKey);
			case REPORT -> reportFileCommandService.findByFileKey(fileKey);
			case DIRECTOR_PROFILE_DETAIL -> directorProfileDetailFileQueryService.findByFileKey(fileKey);
			case BUSINESS_REGISTRATION -> businessRegistrationFileQueryService.findByFileKey(fileKey);
			case CONSULTING_REQUEST -> consultingRequestFileQueryService.findByFileKey(fileKey);
			case CONSULTING_SHEET -> consultingSheetFileQueryService.findByFileKey(fileKey);
			default -> throw new CustomRuntimeException(FileException.INVALID_FILE_KEY);
		};
	}
}
