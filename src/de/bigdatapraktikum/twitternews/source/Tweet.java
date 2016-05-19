package de.bigdatapraktikum.twitternews.source;

import java.time.LocalDateTime;

public class Tweet {
	private long id;
	private LocalDateTime publishedAt;
	private String source;
	private String content;
	private int retweetCount;

	public Tweet(long id, LocalDateTime publishedAt, String source, String content, int retweetCount) {
		super();
		this.id = id;
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

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return id + ";" + publishedAt + ";" + source + ";" + content + ";" + retweetCount;
	}

	public static Tweet fromString(String s) {
		Tweet tweet = null;
		try {
			String[] tweetData = s.split(";");
			tweet = new Tweet(Long.parseLong(tweetData[0]), LocalDateTime.parse(tweetData[1]), tweetData[2],
					tweetData[3], Integer.parseInt(tweetData[4]));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tweet;
	}
}
