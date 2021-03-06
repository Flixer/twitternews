package de.bigdatapraktikum.twitternews.processing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;

import org.junit.Test;

import de.bigdatapraktikum.twitternews.source.Tweet;

public class SettingsTest {

	@Test
	public void testFilter() throws Exception {
		Tweet tweet1 = new Tweet(1, LocalDateTime.of(2016, 5, 20, 0, 0), "SOURCE", "A B C", 1);
		Tweet tweet2 = new Tweet(2, LocalDateTime.of(2016, 5, 25, 0, 0), "SOURCE", "A B", 1);
		Tweet tweet3 = new Tweet(3, LocalDateTime.of(2016, 4, 20, 0, 0), "SOURCE1", "C", 1);
		Tweet tweet4 = new Tweet(4, LocalDateTime.of(2014, 5, 20, 0, 0), "SOURCE", "A C D", 1);
		Tweet tweet5 = new Tweet(5, LocalDateTime.of(2016, 5, 20, 0, 0), "SOURCE", "A B D", 1);

		Settings filter = new Settings();
		filter.setDateFrom("2016-04-20 00:00:00");
		assertTrue(filter.isValidTweet(tweet1));
		assertTrue(filter.isValidTweet(tweet2));
		assertTrue(filter.isValidTweet(tweet3));
		assertFalse(filter.isValidTweet(tweet4));
		assertTrue(filter.isValidTweet(tweet5));

		filter.setDateTo("2016-05-20 00:00:00");
		assertTrue(filter.isValidTweet(tweet1));
		assertFalse(filter.isValidTweet(tweet2));
		assertTrue(filter.isValidTweet(tweet3));
		assertFalse(filter.isValidTweet(tweet4));
		assertTrue(filter.isValidTweet(tweet5));

		filter.setSource("SOURCE");
		assertTrue(filter.isValidTweet(tweet1));
		assertFalse(filter.isValidTweet(tweet2));
		assertFalse(filter.isValidTweet(tweet3));
		assertFalse(filter.isValidTweet(tweet4));
		assertTrue(filter.isValidTweet(tweet5));

		filter.setTweetContent("C");
		assertTrue(filter.isValidTweet(tweet1));
		assertFalse(filter.isValidTweet(tweet2));
		assertFalse(filter.isValidTweet(tweet3));
		assertFalse(filter.isValidTweet(tweet4));
		assertFalse(filter.isValidTweet(tweet5));
	}

}
