package de.bigdatapraktikum.twitternews.source;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;

import org.junit.Test;

public class TweetTest {

	@Test
	public void testFromString() {
		Tweet tweetActual = Tweet.fromString("123;2016-05-19T19:11:56;Source;Content;3");
		Tweet tweetExpected = new Tweet(123, LocalDateTime.of(2016, 05, 19, 19, 11, 56), "Source", "Content", 3);
		assertEquals(tweetExpected, tweetActual);
	}

	@Test
	public void testToString() {
		String tweetStringActual = new Tweet(123, LocalDateTime.of(2016, 05, 19, 19, 11, 56), "Source", "Content", 3)
				.toString();
		String tweetStringExpected = "123;2016-05-19T19:11:56;Source;Content;3";
		assertEquals(tweetStringExpected, tweetStringActual);
	}

}
