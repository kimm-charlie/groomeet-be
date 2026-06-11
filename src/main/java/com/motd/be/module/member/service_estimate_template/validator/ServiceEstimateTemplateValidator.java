package com.motd.be.module.member.service_estimate_template.validator;

import static com.motd.be.common.constants.ValidationConstants.*;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ServiceEstimateTemplateException;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.service_estimate_template.entity.ServiceEstimateTemplate;

@Component
public class ServiceEstimateTemplateValidator {

	public void isOwnedBy(DirectorInfo directorInfo, ServiceEstimateTemplate serviceEstimateTemplate) {
		if (!serviceEstimateTemplate.getDirectorInfo().getId().equals(directorInfo.getId())) {
			throw new CustomRuntimeException(ServiceEstimateTemplateException.NOT_OWNED_BY);
		}
	}

	public void checkCanAddTemplate(long activeTemplateCount) {
		if (activeTemplateCount >= SERVICE_ESTIMATE_TEMPLATE_MAX_LIMIT_COUNT) {
			throw new CustomRuntimeException(ServiceEstimateTemplateException.EXCEEDED_LIMIT_COUNT);
		}
	}
}
