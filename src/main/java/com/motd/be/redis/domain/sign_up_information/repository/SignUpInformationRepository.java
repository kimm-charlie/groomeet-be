package com.motd.be.redis.domain.sign_up_information.repository;

import org.springframework.data.repository.CrudRepository;

import com.motd.be.redis.domain.sign_up_information.entity.SignUpInformation;

public interface SignUpInformationRepository extends CrudRepository<SignUpInformation, String> {
}
