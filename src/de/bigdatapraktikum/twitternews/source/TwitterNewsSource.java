package de.bigdatapraktikum.twitternews.source;

import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.apache.flink.streaming.api.functions.source.SourceFunction;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class TwitterNewsSource implements SourceFunction<Status> {
	private static final long serialVersionUID = 1L;
	private final static int TWITTER_EXCEPTION_RATE_LIMIT = 429;

	private boolean isRunning = false;
	private TreeMap<String, Integer> twitterAccountPagesToCrawl;
	private Date lastExecutionDate;

	/**
	 * Initializes the class with an array of twitter accounts which needs to be
	 * processed
	 * 
	 * @param accountArray
	 * @param lastExecutionDate
	 */
	public TwitterNewsSource(String[] accountArray, Date lastExecutionDate) {
		this.lastExecutionDate = lastExecutionDate;
		twitterAccountPagesToCrawl = new TreeMap<String, Integer>();
		for (String twitterAccount : accountArray) {
			// For each twitter account fill TreeMap with account name as key
			// and 0 as number of current Twitter API user_timeline page as
			// value
			twitterAccountPagesToCrawl.put(twitterAccount, 0);
		}
	}

	@Override
	public void run(SourceContext<Status> context) throws Exception {
		Twitter twitterApi = new TwitterFactory().getInstance();

		isRunning = true;
		while (isRunning) {
			// get twitter account and increment its user_timeline page by one
			String accountName = twitterAccountPagesToCrawl.firstKey();
			int page = twitterAccountPagesToCrawl.get(accountName);
			twitterAccountPagesToCrawl.put(accountName, ++page);

			// read next 200 entries from current user_timeline page
			Paging paging = new Paging(page, 200);
			List<Status> statuses = twitterApi.getUserTimeline(accountName, paging);
			try {
				statuses = twitterApi.getUserTimeline(accountName, paging);
			} catch (TwitterException e) {
				if (e.getStatusCode() == TWITTER_EXCEPTION_RATE_LIMIT) {
					// if we encounter a twitter rate limit, we need to wait for
					// 15 minutes in order to make requests again. To get sure,
					// we will wait 16 minutes
					twitterAccountPagesToCrawl.put(accountName, --page);
					// TODO implement proper logger
					System.out.println("Twitter Rate Limit reached - wait for 16 minutes");
					Thread.sleep(16 * 60 * 1000);
					System.out.println("Resume Twitter crawling");
					break;
				} else {
					// we only treat rate limit errors, otherwise we throw this
					// exception again
					throw e;
				}
			}

			boolean oldTweetDetected = false;
			for (Status status : statuses) {
				if (lastExecutionDate != null && status.getCreatedAt().before(lastExecutionDate)) {
					oldTweetDetected = true;
					break;
				}
				// add status to collector
				context.collect(status);
			}
			// if there are no further tweets, remove the Twitter account name
			// from the TreeMap
			if (statuses.size() == 0 || oldTweetDetected) {
				twitterAccountPagesToCrawl.remove(accountName);
				// if there is no further account to crawl, than set isRunning
				// false
				if (twitterAccountPagesToCrawl.size() == 0) {
					isRunning = false;
				}
			}
		}
	}

	@Override
	public void cancel() {
		isRunning = false;
	}
}
