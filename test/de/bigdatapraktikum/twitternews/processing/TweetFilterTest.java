package de.bigdatapraktikum.twitternews.processing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;

import org.junit.Test;

import de.bigdatapraktikum.twitternews.source.Tweet;

public class TweetFilterTest {

	@Test
	public void testFilterByDateRange() throws Exception {
		Tweet tweet1 = new Tweet(1, LocalDateTime.of(2016, 5, 20, 0, 0), "SOURCE", "A B C", 1);
		Tweet tweet2 = new Tweet(2, LocalDateTime.of(2016, 5, 25, 0, 0), "SOURCE", "A B", 1);
		Tweet tweet3 = new Tweet(3, LocalDateTime.of(2016, 4, 20, 0, 0), "SOURCE", "C", 1);
		Tweet tweet4 = new Tweet(4, LocalDateTime.of(2014, 5, 20, 0, 0), "SOURCE", "A C D", 1);

		TweetFilter filter = new TweetFilter();
		filter.setDateFrom(LocalDateTime.of(2016, 4, 20, 0, 0));
		assertTrue(filter.isValidTweet(tweet1));
		assertTrue(filter.isValidTweet(tweet2));
		assertTrue(filter.isValidTweet(tweet3));
		assertFalse(filter.isValidTweet(tweet4));

		filter.setDateTo(LocalDateTime.of(2016, 5, 20, 0, 0));
		assertTrue(filter.isValidTweet(tweet1));
		assertFalse(filter.isValidTweet(tweet2));
		assertTrue(filter.isValidTweet(tweet3));
		assertFalse(filter.isValidTweet(tweet4));
	}

}
