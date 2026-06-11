package com.motd.be.common.utils;

import java.util.Map;

import com.motd.be.shared.firebase.dto.FirebasePushEvent;

public final class FirebaseCampaignTemplateUtils {
	
	public static String renderTitle(FirebasePushEvent event) {
		return render(
			event.getCampaignSpec().getTitle(),
			event.getVariables()
		);
	}

	public static String renderBody(FirebasePushEvent event) {
		return render(
			event.getCampaignSpec().getBody(),
			event.getVariables()
		);
	}

	private static String render(
		String template,
		Map<String, String> variables
	) {
		if (template == null || variables == null) {
			return template;
		}

		String result = template;
		for (Map.Entry<String, String> entry : variables.entrySet()) {
			result = result.replace(
				"{{" + entry.getKey() + "}}",
				entry.getValue()
			);
		}
		return result;
	}
}
