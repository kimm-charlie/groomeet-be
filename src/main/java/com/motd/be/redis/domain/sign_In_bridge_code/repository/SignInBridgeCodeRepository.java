package com.motd.be.redis.domain.sign_In_bridge_code.repository;

import org.springframework.data.repository.CrudRepository;

import com.motd.be.redis.domain.sign_In_bridge_code.entity.SignInBridgeCode;

public interface SignInBridgeCodeRepository extends CrudRepository<SignInBridgeCode, String> {
}
