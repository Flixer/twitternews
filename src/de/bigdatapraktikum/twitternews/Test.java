package de.bigdatapraktikum.twitternews;

import java.util.List;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class Test {
	public static void main(String[] args) throws TwitterException {
		Twitter twitterApi = new TwitterFactory().getInstance();
		Paging paging = new Paging(3211, 1);
		List<Status> statuses = twitterApi.getUserTimeline("faznet", paging);
		System.out.println(statuses);
	}
}
