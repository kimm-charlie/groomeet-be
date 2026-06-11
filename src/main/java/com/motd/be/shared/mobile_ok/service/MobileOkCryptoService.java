package com.motd.be.shared.mobile_ok.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.dreamsecurity.mobileOK.MobileOKException;
import com.dreamsecurity.mobileOK.mobileOKKeyManager;
import com.motd.be.exception.MobileOkCustomException;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Getter
public class MobileOkCryptoService {

	@Value("${mobile-ok.key-path}")
	private String mobileOkKeyPath;
	@Value("${mobile-ok.secret}")
	private String secret;
	private mobileOKKeyManager mobileOK;

	@PostConstruct
	public void init() {
		mobileOK = new mobileOKKeyManager();
		try {
			mobileOK.keyInit(mobileOkKeyPath, secret);
		} catch (MobileOKException e) {
			throw new MobileOkCustomException("모바일OK 키 초기화 실패", e);
		}
	}

	public String encryptReqClientInfo(String info) {
		try {
			return mobileOK.RSAEncrypt(info);
		} catch (MobileOKException e) {
			throw new MobileOkCustomException("RSA 암호화 실패", e);
		}
	}

	public String getServiceId() {
		return mobileOK.getServiceId();
	}
}
