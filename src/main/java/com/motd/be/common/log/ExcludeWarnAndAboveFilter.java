package com.motd.be.common.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class ExcludeWarnAndAboveFilter extends Filter<ILoggingEvent> {
	@Override
	public FilterReply decide(ILoggingEvent event) {
		// WARN 또는 ERROR 레벨의 로그인 경우, DENY를 반환합니다.
		if (event.getLevel().isGreaterOrEqual(Level.WARN)) {
			return FilterReply.DENY;
		}
		// 그 외의 경우 (INFO, DEBUG, TRACE), ACCEPT를 반환합니다.
		return FilterReply.ACCEPT;
	}
}
