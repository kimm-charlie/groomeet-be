package com.motd.be.module.director.service_estimate_template.controller;

import static com.motd.be.common.constants.Constants.*;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.director.service_estimate_template.dto.request.ServiceEstimateTemplateSaveAndUpdateRequestForDirector;
import com.motd.be.module.director.service_estimate_template.dto.response.ServiceEstimateTemplateFindAllResponseForDirector;
import com.motd.be.module.director.service_estimate_template.dto.response.ServiceEstimateTemplateFindDetailResponseForDirector;
import com.motd.be.module.director.service_estimate_template.facade.ServiceEstimateTemplateFacadeForDirector;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/directors")
public class ServiceEstimateTemplateControllerForDirector {

	private final ServiceEstimateTemplateFacadeForDirector serviceEstimateTemplateFacadeForDirector;

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@GetMapping("/my/services/estimate-templates")
	public ResponseEntity<List<ServiceEstimateTemplateFindAllResponseForDirector>> findAll(
		@RequestParam(value = SERVICE_ID, required = false) Long serviceId,
		@AuthenticationPrincipal Long memberId) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(serviceEstimateTemplateFacadeForDirector.findAll(memberId, serviceId));
	}

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@GetMapping("/my/services/estimate-templates/{templateId}")
	public ResponseEntity<ServiceEstimateTemplateFindDetailResponseForDirector> findDetailByTemplateId(
		@AuthenticationPrincipal Long memberId,
		@PathVariable(TEMPLATE_ID) Long templateId) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(serviceEstimateTemplateFacadeForDirector.findDetailByTemplateId(memberId, templateId));
	}

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@PostMapping("/my/services/estimate-templates")
	public ResponseEntity<Void> save(
		@RequestBody @Validated ServiceEstimateTemplateSaveAndUpdateRequestForDirector request,
		@AuthenticationPrincipal Long memberId) {
		serviceEstimateTemplateFacadeForDirector.save(memberId, request);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@PutMapping("/my/services/estimate-templates/{templateId}")
	public ResponseEntity<ServiceEstimateTemplateFindDetailResponseForDirector> update(
		@RequestBody @Validated ServiceEstimateTemplateSaveAndUpdateRequestForDirector request,
		@AuthenticationPrincipal Long memberId,
		@PathVariable(TEMPLATE_ID) Long templateId) {
		serviceEstimateTemplateFacadeForDirector.update(memberId, request, templateId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@DeleteMapping("/my/services/estimate-templates/{templateId}")
	public ResponseEntity<Void> delete(
		@AuthenticationPrincipal Long memberId,
		@PathVariable(TEMPLATE_ID) Long templateId) {
		serviceEstimateTemplateFacadeForDirector.delete(memberId, templateId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
