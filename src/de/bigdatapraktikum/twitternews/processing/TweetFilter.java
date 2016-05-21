package de.bigdatapraktikum.twitternews.processing;

import java.io.Serializable;
import java.time.LocalDateTime;

import de.bigdatapraktikum.twitternews.source.Tweet;

public class TweetFilter implements Serializable {
	private static final long serialVersionUID = 1L;

	private LocalDateTime dateFrom = null;
	private LocalDateTime dateTo = null;

	public boolean isValidTweet(Tweet tweet) {
		if (dateFrom != null && tweet.getPublishedAt().isBefore(dateFrom)) {
			return false;
		}
		if (dateTo != null && tweet.getPublishedAt().isAfter(dateTo)) {
			return false;
		}

		return true;
	}

	public LocalDateTime getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(LocalDateTime dateFrom) {
		this.dateFrom = dateFrom;
	}

	public LocalDateTime getDateTo() {
		return dateTo;
	}

	public void setDateTo(LocalDateTime dateTo) {
		this.dateTo = dateTo;
	}
}
