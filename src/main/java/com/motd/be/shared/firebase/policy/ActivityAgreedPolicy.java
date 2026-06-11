package com.motd.be.shared.firebase.policy;

public class ActivityAgreedPolicy implements PushSendPolicy {

	public boolean canSend(PushContext ctx) {
		return ctx.getReceiver().getIsActivityPushAgreed();
	}
}
