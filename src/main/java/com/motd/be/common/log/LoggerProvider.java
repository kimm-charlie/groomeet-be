package com.motd.be.common.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggerProvider {

	private static final Logger schedulerLogger = LoggerFactory.getLogger("SchedulerLogger");
	private static final Logger devLogger = LoggerFactory.getLogger("DevLogger");

	public Logger getSchedulerLogger() {
		return schedulerLogger;
	}

	public Logger getDevLogger() {
		return devLogger;
	}
}
