package com.motd.be.common.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class AllowWarnOnlyFilter extends Filter<ILoggingEvent> {
	@Override
	public FilterReply decide(ILoggingEvent event) {
		// ERROR 레벨의 로그만 허용합니다.
		if (event.getLevel().equals(Level.WARN)) {
			return FilterReply.ACCEPT;
		}
		// 그 외의 경우 (WARN, INFO, DEBUG, TRACE), DENY를 반환합니다.
		return FilterReply.DENY;
	}
}
