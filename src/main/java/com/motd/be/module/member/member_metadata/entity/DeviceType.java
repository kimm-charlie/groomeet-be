package com.motd.be.module.member.member_metadata.entity;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.DeviceTypeException;

public enum DeviceType {
	ANDROID, IOS, WEB;

	public static DeviceType fromString(String deviceType) {
		for (DeviceType dt : DeviceType.values()) {
			if (dt.name().equalsIgnoreCase(deviceType)) {
				return dt;
			}
		}
		throw new CustomRuntimeException(DeviceTypeException.NOT_FOUND);
	}
}
