package com.motd.be.shared.hackle.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.HackleGenericException;
import com.motd.be.exception.exceptions.HackleSpecificException;
import com.motd.be.shared.hackle.dto.response.HackleErrorResponse;

import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HackleFeignErrorDecoder implements ErrorDecoder {

	private final ObjectMapper objectMapper;

	/**
	 * 1. body에 hackle error code가 있으면 → SPECIFIC
	 * 2. body가 없으면 → GENERIC(status)
	 * 3. 둘 다 없으면 → UNKNOWN
	 *
	 * @param methodKey
	 * @param response
	 * @return
	 */
	@Override
	public Exception decode(String methodKey, Response response) {
		String body = null;

		try {
			if (response.body() != null) {
				body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
			}

			// 1. body가 있고 hackle code가 있으면 → 구체 에러
			if (body != null && !body.isBlank()) {
				try {
					HackleErrorResponse error =
						objectMapper.readValue(body, HackleErrorResponse.class);

					if (error.getCode() != null) {
						HackleSpecificException specificException =
							HackleSpecificException.from(error.getCode());

						if (specificException != null) {
							return new CustomRuntimeException(specificException);
						}
					}
				} catch (Exception ignore) {
				}
			}

			// 2. body 정보가 없으면 → status 기반 범주 에러
			return new CustomRuntimeException(
				HackleGenericException.fromHttpStatus(response.status())
			);

		} catch (IOException e) {
			return new CustomRuntimeException(
				HackleGenericException.UNKNOWN_ERROR
			);
		}
	}
}
