package com.motd.be.module.member.director_info.validator;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.DirectorInfoException;
import com.motd.be.module.member.member.entity.Member;

@Component
public class DirectorInfoValidator {

	public void validateNotAlreadyDirector(Member member) {
		if (member.isDirector()) {
			throw new CustomRuntimeException(DirectorInfoException.ALREADY_DIRECTOR);
		}
	}
}
