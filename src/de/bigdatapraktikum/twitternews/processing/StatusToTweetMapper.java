package de.bigdatapraktikum.twitternews.processing;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.apache.flink.api.common.functions.MapFunction;

import de.bigdatapraktikum.twitternews.source.Tweet;
import twitter4j.Status;

public class StatusToTweetMapper implements MapFunction<Status, Tweet> {
	private static final long serialVersionUID = 1L;

	/**
	 * Maps a twitter4j.Status object into a
	 * de.bigdatapraktikum.twitternews.source.Tweet object
	 * 
	 * @param status
	 *            The Status object which needs to be converted
	 * @return Tweet The Tweet object with the information from the Status
	 *         object
	 */
	@Override
	public Tweet map(Status status) throws Exception {
		// replace semicolon and new lines from content and source in order to
		// save it later properly
		String content = status.getText();
		content = content.replaceAll("[;\r\n]", "");
		String source = status.getUser().getName();
		source = source.replaceAll("[;\r\n]", "");
		// fill Tweet object with content from the twitter4j.Status Object
		Tweet t = new Tweet(LocalDateTime.ofInstant(status.getCreatedAt().toInstant(), ZoneId.systemDefault()), source,
				content, status.getRetweetCount());
		return t;
	}

}
