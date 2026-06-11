package com.motd.be.module.admin.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class AuthAdminSignInRequest {

	@NotBlank(message = "이메일은 필수입니다.")
	private String email;
	@NotBlank(message = "비밀번호는 필수입니다.")
	private String password;
}
