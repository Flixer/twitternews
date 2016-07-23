package de.bigdatapraktikum.twitternews.parts;

import java.util.ArrayList;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.operators.Order;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.io.TextOutputFormat.TextFormatter;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.core.fs.FileSystem.WriteMode;
import org.apache.flink.util.Collector;

import de.bigdatapraktikum.twitternews.processing.IdfValueCalculator;
import de.bigdatapraktikum.twitternews.processing.TweetFilter;
import de.bigdatapraktikum.twitternews.processing.UniqueWordMapper;
import de.bigdatapraktikum.twitternews.processing.UniqueWordsIdfJoin;
import de.bigdatapraktikum.twitternews.source.Tweet;
import de.bigdatapraktikum.twitternews.utils.AppConfig;

public class TwitterNewsTopicAnalysis {
	public DataSet<Tuple2<Tweet, ArrayList<String>>> getFilteredWordsInTweets(ExecutionEnvironment env,
			TweetFilter filter) throws Exception {

		// get input data from previously stored twitter data
		DataSource<String> tweetStrings = env.readTextFile(AppConfig.RESOURCES_TWEETS, "UTF-8");
		DataSet<Tweet> tweets = tweetStrings.flatMap(new FlatMapFunction<String, Tweet>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void flatMap(String value, Collector<Tweet> out) throws Exception {
				Tweet tweet = Tweet.fromString(value);
				// currently this filter can filter time range
				// later this filter function could be upgraded with different
				// filter-properties
				if (filter.isValidTweet(tweet)) {
					out.collect(tweet);
				}
			}
		});
		// Calculates the number of tweets
		double amountOfTweets = tweets.count();

		// Calculates occurrence for all the unique words. Excludes the
		// irrelevant words that are defined in the AppConfig.java
		DataSet<Tuple3<Tweet, String, Integer>> uniqueWordsinTweets = tweets
				.flatMap(new UniqueWordMapper(AppConfig.IRRELEVANT_WORDS));

		// group all unique words in tweets and get their respective number of
		// occurences
		DataSet<Tuple2<String, Integer>> tweetFrequency = uniqueWordsinTweets
				.map(new MapFunction<Tuple3<Tweet, String, Integer>, Tuple2<String, Integer>>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Tuple2<String, Integer> map(Tuple3<Tweet, String, Integer> input) throws Exception {
						String uniqueWord = input.f1;
						Integer count = input.f2;
						return new Tuple2<>(uniqueWord, count);
					}
				}).groupBy(0).sum(1);

		// For Testing
		// Prints all the unique words with their occurrence in descending order
		// tweetFrequency.filter(new FilterFunction<Tuple2<String, Integer>>() {
		// private static final long serialVersionUID = 1L;
		//
		// @Override
		// public boolean filter(Tuple2<String, Integer> word) throws Exception
		// {
		// return word.f1 > 50;
		// }
		// }).sortPartition(1, Order.DESCENDING).print();

		// Calculates the IDF Values for all the words
		DataSet<Tuple2<String, Double>> idfValues = tweetFrequency.map(new IdfValueCalculator(amountOfTweets));

		// DEPRECATED: filter words by MAX_IDF_VALUE -> isn't used any more
		// because maximum idf value is connected with the total number of
		// tweets

		// DataSet<Tuple2<String, Double>> filteredIdfValues = idfValues
		// .filter(new FilterFunction<Tuple2<String, Double>>() {
		// private static final long serialVersionUID = 1L;
		//
		// @Override
		// public boolean filter(Tuple2<String, Double> word) throws Exception {
		// return word.f1 < AppConfig.MAX_IDF_VALUE;
		// }
		// });

		// get the first n entries with the highest idf value
		DataSet<Tuple2<String, Double>> filteredIdfValues = idfValues.sortPartition(1, Order.ASCENDING)
				.first(AppConfig.NUMBER_OF_NODES);
		double maxIdfValue = filteredIdfValues.max(1).collect().get(0).f1;
		filteredIdfValues.writeAsFormattedText(AppConfig.RESOURCES_GRAPH_NODES, WriteMode.OVERWRITE,
				new TextFormatter<Tuple2<String, Double>>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String format(Tuple2<String, Double> value) {
						return "{\"data\":{\"id\":\"" + value.f0 + "\",\"name\":\"" + value.f0 + "\",\"score\":"
								+ (1 - value.f1 / maxIdfValue) + "},\"group\":\"nodes\"},";
					}
				});

		// Join unique words in tweets with filteredIdfValue words, so that the
		// resulting data is a dataset with tuple2 objects (which contain a
		// tweet and a topic word within that tweet). We group that data by
		// tweet and aggregate all topic words in an ArrayList. The final result
		// is a dataset with tweets and a list of all topic words within that
		// tweet
		DataSet<Tuple2<Tweet, ArrayList<String>>> wordsPerTweet = uniqueWordsinTweets.join(filteredIdfValues).where(1)
				.equalTo(0).with(new UniqueWordsIdfJoin()).groupBy(new KeySelector<Tuple2<Tweet, String>, Long>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Long getKey(Tuple2<Tweet, String> value) throws Exception {
						return value.f0.getId();
					}
				}).reduceGroup(new GroupReduceFunction<Tuple2<Tweet, String>, Tuple2<Tweet, ArrayList<String>>>() {
					private static final long serialVersionUID = 1L;

					@Override
					public void reduce(Iterable<Tuple2<Tweet, String>> values,
							Collector<Tuple2<Tweet, ArrayList<String>>> out) throws Exception {
						// reduce data like that:
						// ------------------------
						// tweet-1 -> word1
						// tweet-1 -> word2
						// tweet-2 -> word1
						//
						// to:
						// ------------------------
						// tweet-1 -> (word1, word2)
						// tweet-2 -> (word1)

						Tweet tweet = null;
						ArrayList<String> wordList = new ArrayList<>();
						for (Tuple2<Tweet, String> t : values) {
							tweet = t.f0;
							wordList.add(t.f1);
						}
						out.collect(new Tuple2<Tweet, ArrayList<String>>(tweet, wordList));
					}
				});
		// wordsPerTweet.print();

		return wordsPerTweet;
	}
}