package com.motd.be.shared.firebase.policy;

public class ChatVisibilityPolicy implements PushSendPolicy {
	public boolean canSend(PushContext ctx) {
		return Boolean.TRUE.equals(ctx.getChatMessage().getIsVisibleToOpponent());
	}
}
