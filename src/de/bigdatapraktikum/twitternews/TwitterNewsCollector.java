package de.bigdatapraktikum.twitternews;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import de.bigdatapraktikum.twitternews.processing.StatusToTweetMapper;
import de.bigdatapraktikum.twitternews.source.Tweet;
import de.bigdatapraktikum.twitternews.source.TwitterNewsSource;
import de.bigdatapraktikum.twitternews.utils.AppConfig;
import twitter4j.Status;

public class TwitterNewsCollector {

	public static void main(String[] args) throws Exception {
		Date lastExecutionDate = new Date(new File(AppConfig.RESOURCES_TWEETS_TXT).lastModified());

		// set up the execution environment
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		// get input data (tweet Status objects) from specific twitter accounts
		DataStreamSource<Status> tweetStatuses = env
				.addSource(new TwitterNewsSource(AppConfig.TWITTER_ACCOUNTS_TO_CRAWL, lastExecutionDate));

		// map twitter Status objects to Tweet objects
		SingleOutputStreamOperator<Tweet> tweets = tweetStatuses.map(new StatusToTweetMapper());

		// save tweets in custom sync
		// we don't use standard writeAsText() method because it doesn't support
		// file appending

		tweets.addSink(new SinkFunction<Tweet>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void invoke(Tweet tweet) throws Exception {
				FileWriter fileWritter = new FileWriter(AppConfig.RESOURCES_TWEETS_TXT, true);
				BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
				bufferWritter.write(tweet.toString());
				bufferWritter.newLine();
				bufferWritter.close();
			}
		});

		// run application
		env.execute();
	}
}
