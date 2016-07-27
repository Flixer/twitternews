package de.bigdatapraktikum.twitternews.parts;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.operators.Order;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.graph.Graph;
import org.apache.flink.graph.Vertex;
import org.apache.flink.graph.library.CommunityDetection;
import org.apache.flink.util.Collector;

import de.bigdatapraktikum.twitternews.config.AppConfig;
import de.bigdatapraktikum.twitternews.output.OutputCluster;
import de.bigdatapraktikum.twitternews.output.OutputEdges;
import de.bigdatapraktikum.twitternews.output.OutputHistorical;
import de.bigdatapraktikum.twitternews.output.OutputNodes;
import de.bigdatapraktikum.twitternews.processing.ChineseWhisper;
import de.bigdatapraktikum.twitternews.processing.EdgeMapper;
import de.bigdatapraktikum.twitternews.processing.IdfValueCalculator;
import de.bigdatapraktikum.twitternews.processing.InitialNodeClassMapper;
import de.bigdatapraktikum.twitternews.processing.Settings;
import de.bigdatapraktikum.twitternews.processing.Settings.ClusterAlgorithms;
import de.bigdatapraktikum.twitternews.processing.TweetGroupJoin;
import de.bigdatapraktikum.twitternews.processing.UniqueWordMapper;
import de.bigdatapraktikum.twitternews.processing.UniqueWordsIdfJoin;
import de.bigdatapraktikum.twitternews.source.Tweet;

// this class creates a co-occurrence graph
public class TwitterNewsGraphCreator {
	public void execute(Settings settings) throws Exception {
		// TODO: add more comments to explain what is happening in this class

		final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		env.setParallelism(AppConfig.PARALLELISM);

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
				if (settings.isValidTweet(tweet)) {
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
				}).groupBy(0).sum(1).setParallelism(1);

		// Calculates the IDF Values for all the words
		DataSet<Tuple2<String, Double>> idfValues = tweetFrequency.map(new IdfValueCalculator(amountOfTweets));

		// get the first n entries with the highest idf value
		DataSet<Tuple2<String, Double>> filteredIdfValues = idfValues.sortPartition(1, Order.ASCENDING)
				.first(AppConfig.NUMBER_OF_NODES);

		// Join unique words in tweets with filteredIdfValue words, so that the
		// resulting data is a dataset with tuple2 objects (which contain a
		// tweet and a topic word within that tweet). We group that data by
		// tweet and aggregate all topic words in an ArrayList. The final result
		// is a dataset with tweets and a list of all topic words within that
		// tweet
		DataSet<Tuple2<Tweet, String>> wordsPerTweet = uniqueWordsinTweets.join(filteredIdfValues).where(1).equalTo(0)
				.with(new UniqueWordsIdfJoin());
		// wordsPerTweet.print();

		DataSet<Tuple2<Tweet, ArrayList<String>>> wordsPerTweetList = wordsPerTweet
				.groupBy(new KeySelector<Tuple2<Tweet, String>, Long>() {
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

		DataSet<Tuple2<String, HashMap<String, Integer>>> dateWithSourcesDistribution = wordsPerTweetList
				.groupBy(new KeySelector<Tuple2<Tweet, ArrayList<String>>, String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String getKey(Tuple2<Tweet, ArrayList<String>> value) throws Exception {
						return value.f0.getPublishedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
					}
				}).reduceGroup(
						new GroupReduceFunction<Tuple2<Tweet, ArrayList<String>>, Tuple2<String, HashMap<String, Integer>>>() {
							private static final long serialVersionUID = 1L;

							@Override
							public void reduce(Iterable<Tuple2<Tweet, ArrayList<String>>> values,
									Collector<Tuple2<String, HashMap<String, Integer>>> out) throws Exception {
								String date = null;
								HashMap<String, Integer> sources = new HashMap<>();
								for (Tuple2<Tweet, ArrayList<String>> v : values) {
									date = v.f0.getPublishedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
									sources.put(v.f0.getSource(), sources.getOrDefault(v.f0.getSource(), 0) + 1);
								}
								out.collect(new Tuple2<String, HashMap<String, Integer>>(date, sources));
							}
						});

		// create the graph
		DataSet<Tuple3<String, String, Double>> edges = wordsPerTweetList.flatMap(new EdgeMapper()).groupBy(0, 1)
				.sum(2);

		Graph<String, Long, Double> graph = Graph.fromTupleDataSet(edges, new InitialNodeClassMapper(), env);

		Graph<String, Long, Double> graphWithClusterId = null;
		if (settings.getClusterAlgorithm().equals(ClusterAlgorithms.CHINESE_WHISPER.getValue())) {
			graphWithClusterId = graph.run(new ChineseWhisper<String>(settings.getClusterIterationCount()));
		} else {
			graphWithClusterId = graph
					.run(new CommunityDetection<String>(settings.getClusterIterationCount(), AppConfig.delta));
		}

		DataSet<Vertex<String, Long>> vertexWithClusterId = graphWithClusterId.getVertices();

		DataSet<Tuple2<Long, ArrayList<String>>> clusterIdWithWords = graphWithClusterId.getVertices().groupBy(1)
				.reduceGroup(new GroupReduceFunction<Vertex<String, Long>, Tuple2<Long, ArrayList<String>>>() {
					private static final long serialVersionUID = 1L;

					@Override
					public void reduce(Iterable<Vertex<String, Long>> values,
							Collector<Tuple2<Long, ArrayList<String>>> out) throws Exception {

						Long clusterId = null;
						ArrayList<String> wordList = new ArrayList<>();
						for (Tuple2<String, Long> t : values) {
							clusterId = t.f1;
							wordList.add(t.f0);
						}
						if (wordList.size() > 1) {
							// we don't want 1-word cluster
							out.collect(new Tuple2<Long, ArrayList<String>>(clusterId, wordList));
						}
					}
				});
		// tweetId, source, clusterId
		DataSet<Tuple3<Long, String, Long>> tweetIdWithSourceAndClusterId = wordsPerTweet.join(vertexWithClusterId)
				.where(1).equalTo(0).with(new TweetGroupJoin());

		DataSet<Tuple3<String, Long, Long>> sourceWithClusterIdAndSize = tweetIdWithSourceAndClusterId.groupBy(2, 1)
				.reduceGroup(new GroupReduceFunction<Tuple3<Long, String, Long>, Tuple3<String, Long, Long>>() {
					private static final long serialVersionUID = 1L;

					@Override
					public void reduce(Iterable<Tuple3<Long, String, Long>> values,
							Collector<Tuple3<String, Long, Long>> out) throws Exception {
						HashSet<Long> hs = new HashSet<>();
						Long clusterId = null;
						String source = "";
						for (Tuple3<Long, String, Long> v : values) {
							hs.add(v.f0);
							source = v.f1;
							clusterId = v.f2;

						}
						out.collect(new Tuple3<String, Long, Long>(source, clusterId, (long) hs.size()));
					}

				});
		DataSet<Tuple3<Long, ArrayList<String>, ArrayList<Tuple2<String, Long>>>> groupsWithWordsAndSources = clusterIdWithWords
				.join(sourceWithClusterIdAndSize).where(0).equalTo(1).groupBy("f0.f0").reduceGroup(
						new GroupReduceFunction<Tuple2<Tuple2<Long, ArrayList<String>>, Tuple3<String, Long, Long>>, Tuple3<Long, ArrayList<String>, ArrayList<Tuple2<String, Long>>>>() {
							private static final long serialVersionUID = 1L;

							@Override
							public void reduce(
									Iterable<Tuple2<Tuple2<Long, ArrayList<String>>, Tuple3<String, Long, Long>>> values,
									Collector<Tuple3<Long, ArrayList<String>, ArrayList<Tuple2<String, Long>>>> out)
									throws Exception {
								Long groupId = null;
								ArrayList<String> wordList = new ArrayList<>();
								ArrayList<Tuple2<String, Long>> sourcesList = new ArrayList<>();
								for (Tuple2<Tuple2<Long, ArrayList<String>>, Tuple3<String, Long, Long>> v : values) {
									sourcesList.add(new Tuple2<String, Long>(v.f1.f0, v.f1.f2));
									wordList = v.f0.f1;
									groupId = v.f0.f0;
								}

								out.collect(new Tuple3<Long, ArrayList<String>, ArrayList<Tuple2<String, Long>>>(
										groupId, wordList, sourcesList));
							}

						});

		OutputHistorical.set(dateWithSourcesDistribution);
		OutputNodes.set(filteredIdfValues, "");
		OutputEdges.set(graph.getEdges());
		OutputCluster.set(groupsWithWordsAndSources);

		// env.execute();
	}

}
