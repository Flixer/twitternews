package de.bigdatapraktikum.twitternews;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import de.bigdatapraktikum.twitternews.processing.StatusToTweetMapper;
import de.bigdatapraktikum.twitternews.source.Tweet;
import de.bigdatapraktikum.twitternews.source.TwitterNewsSource;
import de.bigdatapraktikum.twitternews.utils.AppConfig;
import twitter4j.Status;

public class TwitterNewsCollector {

	public static void main(String[] args) throws Exception {
		
		// TODO: Implement that Tweets can be collected over a time period
		
		// set up the execution environment
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		// get input data (tweet Status objects) from specific twitter accounts
		DataStreamSource<Status> tweetStatuses = env
				.addSource(new TwitterNewsSource(AppConfig.TWITTER_ACCOUNTS_TO_CRAWL));

		// map twitter Status objects to Tweet objects
		SingleOutputStreamOperator<Tweet> tweets = tweetStatuses.map(new StatusToTweetMapper());

		// save tweets
		tweets.writeAsText(AppConfig.TWEET_STORAGE_PATH);

		// run application
		env.execute();
	}
}
