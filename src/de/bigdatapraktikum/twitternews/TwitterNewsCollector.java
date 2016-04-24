package de.bigdatapraktikum.twitternews;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import de.bigdatapraktikum.twitternews.processing.StatusToTweetMapper;
import de.bigdatapraktikum.twitternews.source.Tweet;
import de.bigdatapraktikum.twitternews.source.TwitterNewsSource;
import twitter4j.Status;

public class TwitterNewsCollector {
	private static final String[] twitterAccountsToCrawl = new String[] { "faznet", "SPIEGELONLINE", "SZ",
			"jungewelt" };

	public static void main(String[] args) throws Exception {
		// set up the execution environment
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		// get input data (tweet Status objects) from specific twitter accounts
		DataStreamSource<Status> tweetStatuses = env.addSource(new TwitterNewsSource(twitterAccountsToCrawl));

		// map twitter Status objects to Tweet objects
		SingleOutputStreamOperator<Tweet> tweets = tweetStatuses.map(new StatusToTweetMapper());

		// save tweets
		tweets.writeAsText("resources/tweet_storage");

		// run application
		env.execute();
	}
}
