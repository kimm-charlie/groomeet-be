package com.motd.be.shared.firebase.policy;

public interface PushSendPolicy {

	boolean canSend(PushContext context);
}
