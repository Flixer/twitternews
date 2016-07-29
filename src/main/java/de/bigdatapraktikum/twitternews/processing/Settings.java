package de.bigdatapraktikum.twitternews.processing;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import de.bigdatapraktikum.twitternews.source.Tweet;

public class Settings implements Serializable {
	private static final long serialVersionUID = 1L;

	private LocalDateTime dateFrom = null;
	private LocalDateTime dateTo = null;
	private String source = null;
	private String tweetContent = null;

	private int clusterIterationCount = 5;
	private String clusterAlgorithm = ClusterAlgorithms.COMMUNITY_DETECTION.getValue();

	public enum ClusterAlgorithms {
		CHINESE_WHISPER("chinese_whisper"), COMMUNITY_DETECTION("community_detection");
		private final String value;

		private ClusterAlgorithms(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	};

	public boolean isValidTweet(Tweet tweet) {
		if (dateFrom != null && tweet.getPublishedAt().isBefore(dateFrom)) {
			return false;
		}
		if (dateTo != null && tweet.getPublishedAt().isAfter(dateTo)) {
			return false;
		}
		if (source != null && (tweet.getSource() == null || !tweet.getSource().equals(source))) {
			return false;
		}
		if (tweetContent != null
				&& (tweet.getContent() == null || !tweet.getContent().toLowerCase().contains(tweetContent))) {
			return false;
		}

		return true;
	}

	public LocalDateTime getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(String dateFrom) {
		this.dateFrom = LocalDateTime.parse(dateFrom, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	}

	public LocalDateTime getDateTo() {
		return dateTo;
	}

	public void setDateTo(String dateTo) {
		this.dateTo = LocalDateTime.parse(dateTo, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getTweetContent() {
		return tweetContent;
	}

	public void setTweetContent(String tweetContent) {
		this.tweetContent = tweetContent.toLowerCase();
	}

	public int getClusterIterationCount() {
		return clusterIterationCount;
	}

	public void setClusterIterationCount(int clusterIterationCount) {
		this.clusterIterationCount = clusterIterationCount;
	}

	public String getClusterAlgorithm() {
		return clusterAlgorithm;
	}

	public void setClusterAlgorithm(String clusterAlgorithm) {
		this.clusterAlgorithm = clusterAlgorithm;
	}
}
