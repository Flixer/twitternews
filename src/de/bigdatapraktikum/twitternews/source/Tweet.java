package de.bigdatapraktikum.twitternews.source;

import java.time.LocalDateTime;

public class Tweet {
	LocalDateTime publishedAt;
	String source;
	String content;
	int retweetCount;

	public Tweet(LocalDateTime publishedAt, String source, String content, int retweetCount) {
		super();
		this.publishedAt = publishedAt;
		this.source = source;
		this.content = content;
		this.retweetCount = retweetCount;
	}

	@Override
	public String toString() {
		return "Tweet [publishedAt=" + publishedAt + ", source=" + source + ", content=" + content + ", retweetCount="
				+ retweetCount + "]";
	}
}
