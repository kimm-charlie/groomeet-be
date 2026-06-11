package com.motd.be.shared.firebase.policy;

public class ReceiverOfflinePolicy implements PushSendPolicy {
	public boolean canSend(PushContext ctx) {
		return !ctx.isReceiverOnline();
	}
}
