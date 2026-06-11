package com.motd.be.common.utils;

import static com.motd.be.common.constants.Constants.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class MobileOkUtils {

	public static String generateClientTxId() {
		return CLIENT_PREFIX + UUID.randomUUID().toString().replace("-", "");
	}

	public static String buildReqClientInfo(String clientTxId) {
		return clientTxId + "|" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
	}
}
