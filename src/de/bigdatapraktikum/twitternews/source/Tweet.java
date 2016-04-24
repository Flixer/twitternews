package de.bigdatapraktikum.twitternews.source;

import java.time.LocalDateTime;

public class Tweet {
	private LocalDateTime publishedAt;
	private String source;
	private String content;
	private int retweetCount;

	public Tweet(LocalDateTime publishedAt, String source, String content, int retweetCount) {
		super();
		this.publishedAt = publishedAt;
		this.source = source;
		this.content = content;
		this.retweetCount = retweetCount;
	}

	public LocalDateTime getPublishedAt() {
		return publishedAt;
	}

	public void setPublishedAt(LocalDateTime publishedAt) {
		this.publishedAt = publishedAt;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getRetweetCount() {
		return retweetCount;
	}

	public void setRetweetCount(int retweetCount) {
		this.retweetCount = retweetCount;
	}

	@Override
	public String toString() {
		return publishedAt + ";" + source + ";" + content + ";" + retweetCount;
	}
}
