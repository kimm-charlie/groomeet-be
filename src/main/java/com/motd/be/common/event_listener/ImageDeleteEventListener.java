package com.motd.be.common.event_listener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.motd.be.shared.aws.enums.S3DirectoryType;
import com.motd.be.shared.aws.service.S3Uploader;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ImageDeleteEventListener {

	private final S3Uploader s3Uploader;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(ImageDeleteEvent event) {
		s3Uploader.delete(event.url(), event.directory());
	}

	public record ImageDeleteEvent(String url, S3DirectoryType directory) {
	}
}
