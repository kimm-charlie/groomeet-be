package com.motd.be.shared.ai.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AiChatCompletionRequest {

	private String model;
	private List<Message> messages;

	@Getter
	@Builder
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Message {
		private String role;
		private Object content;

		public static Message ofText(String role, String text) {
			return Message.builder()
				.role(role)
				.content(text)
				.build();
		}

		public static Message ofMultiContent(String role, List<Content> contents) {
			return Message.builder()
				.role(role)
				.content(contents)
				.build();
		}
	}

	@Getter
	@Builder
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Content {
		private String type;
		private String text;
		@JsonProperty("image_url")
		private ImageUrl imageUrl;

		public static Content ofText(String text) {
			return Content.builder()
				.type("text")
				.text(text)
				.build();
		}

		public static Content ofImageUrl(String url) {
			return Content.builder()
				.type("image_url")
				.imageUrl(ImageUrl.builder().url(url).build())
				.build();
		}
	}

	@Getter
	@Builder
	public static class ImageUrl {
		private String url;
	}
}
