package de.bigdatapraktikum.twitternews.parts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.io.TextOutputFormat.TextFormatter;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.core.fs.FileSystem.WriteMode;
import org.apache.flink.graph.Edge;
import org.apache.flink.graph.Graph;
import org.apache.flink.graph.Vertex;
import org.apache.flink.graph.library.CommunityDetection;
import org.apache.flink.util.Collector;

import de.bigdatapraktikum.twitternews.processing.EdgeMapper;
import de.bigdatapraktikum.twitternews.processing.InitialNodeClassMapper;
import de.bigdatapraktikum.twitternews.processing.TweetFilter;
import de.bigdatapraktikum.twitternews.processing.TweetGroupJoin;
import de.bigdatapraktikum.twitternews.source.Tweet;
import de.bigdatapraktikum.twitternews.utils.AppConfig;

// this class creates a co-occurrence graph
public class TwitterNewsGraphCreator {
	private Graph<String, Long, Double> graph;
	private List<Vertex<String, Long>> verticleList;

	public void execute(TweetFilter tweetFilter) throws Exception {
		// tweetFilter.setDateFrom(LocalDateTime.now().minusDays(7));
		// tweetFilter.setDateTo(LocalDateTime.now().minusHours(0));

		final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		env.setParallelism(1);

		//
		// DataSet<Tuple2<Integer, String>> test =
		// env.fromCollection(test1).map(new MapFunction<Integer,
		// Tuple2<Integer, String>>() {
		//
		// @Override
		// public Tuple2<Integer, String> map(Integer arg0) throws Exception {
		// // TODO Auto-generated method stub
		// return new Tuple2<>(arg0, "Test" + arg0);
		// }
		// });
		// test.sortPartition(0, Order.ASCENDING).print();

		// returns the co-occurrence graph with the help of the
		// TwitterNewsTopicAnalysis
		// public Graph<String, NullValue, Integer> getCoOccurrenceGraph()
		// throws Exception{

		// get the filtered tweets
		TwitterNewsTopicAnalysis twitterNewsTopicAnalysis = new TwitterNewsTopicAnalysis();
		DataSet<Tuple2<Tweet, String>> wordsPerTweet = twitterNewsTopicAnalysis.getFilteredWordsInTweets(env,
				tweetFilter);

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

		// create the graph
		DataSet<Tuple3<String, String, Double>> edges = wordsPerTweetList.flatMap(new EdgeMapper()).groupBy(0, 1)
				.sum(2);

		graph = Graph.fromTupleDataSet(edges, new InitialNodeClassMapper(), env);

		// get the strongest connection between two nodes
		double maxEdgeCount = graph.getEdges().max(2).collect().get(0).f2;

		graph.getEdges().writeAsFormattedText(AppConfig.RESOURCES_GRAPH_EDGES, WriteMode.OVERWRITE,
				new TextFormatter<Edge<String, Double>>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String format(Edge<String, Double> value) {
						double weight = Math.min(value.f2 / maxEdgeCount, 1);
						int colorIntensityR = (int) (180. + (75 * weight));
						int colorIntensityG = (int) (180. * (1. - weight));
						int colorIntensityB = (int) (110. * (1. - weight));
						return "{\"data\":{\"source\":\"" + value.f0 + "\",\"target\":\"" + value.f1 + "\",\"weight\":"
								+ weight + "},\"group\":\"edges\",\"style\":{\"line-color\":\"rgb(" + colorIntensityR
								+ ", " + colorIntensityG + "," + colorIntensityB + ")\"}},";
					}
				});

		verticleList = graph.getVertices().collect();
		Graph<String, Long, Double> graphWithClusterId = graph
				.run(new CommunityDetection<String>(AppConfig.maxIterations, AppConfig.delta));
		DataSet<Vertex<String, Long>> vertexWithGroup = graphWithClusterId.getVertices();

		DataSet<Tuple2<Long, ArrayList<String>>> groupsWithWords = graphWithClusterId.getVertices().groupBy(1)
				.reduceGroup(new GroupReduceFunction<Vertex<String, Long>, Tuple2<Long, ArrayList<String>>>() {
					private static final long serialVersionUID = 1L;

					@Override
					public void reduce(Iterable<Vertex<String, Long>> values,
							Collector<Tuple2<Long, ArrayList<String>>> out) throws Exception {

						Long group = null;
						ArrayList<String> wordList = new ArrayList<>();
						for (Tuple2<String, Long> t : values) {
							group = t.f1;
							wordList.add(t.f0);
						}
						out.collect(new Tuple2<Long, ArrayList<String>>(group, wordList));
					}
				});
		// ID, Source, Gruppe
		DataSet<Tuple3<Long, String, Long>> tweetsWithGroup = wordsPerTweet.join(vertexWithGroup).where(1).equalTo(0)
				.with(new TweetGroupJoin());

		DataSet<Tuple3<String, Long, Long>> sourceGroupSize = tweetsWithGroup.groupBy(2, 1)
				.reduceGroup(new GroupReduceFunction<Tuple3<Long, String, Long>, Tuple3<String, Long, Long>>() {
					private static final long serialVersionUID = 1L;

					@Override
					public void reduce(Iterable<Tuple3<Long, String, Long>> values,
							Collector<Tuple3<String, Long, Long>> out) throws Exception {
						HashSet<Long> hs = new HashSet<>();
						Long group = 0l;
						String source = "";
						for (Tuple3<Long, String, Long> v : values) {
							hs.add(v.f0);
							source = v.f1;
							group = v.f2;

						}
						out.collect(new Tuple3<String, Long, Long>(source, group, (long) hs.size()));
					}

				});
		// groupsWithWords.join(sourceGroupSize).where(0).equalTo(1).groupBy("f0.f0").reduceGroup(
		// new GroupReduceFunction<Tuple2<Tuple2<Long,
		// ArrayList<String>>, Tuple3<String, Long, Long>>, Tuple3<Long,
		// ArrayList<String>, Tuple2<String, Long>>>() {
		// private static final long serialVersionUID = 1L;
		//
		// @Override
		// public void reduce(
		// Iterable<Tuple2<Tuple2<Long, ArrayList<String>>,
		// Tuple3<String, Long, Long>>> values,
		// Collector<Tuple3<Long, ArrayList<String>, Tuple2<String,
		// Long>>> out) throws Exception {
		//
		// for (Tuple2<Tuple2<Long, ArrayList<String>>, Tuple3<String,
		// Long, Long>> v : values) {
		//
		// }
		//
		// }
		//
		// });

		groupsWithWords.writeAsFormattedText(AppConfig.RESOURCES_GRAPH_CLUSTER, WriteMode.OVERWRITE,
				new TextFormatter<Tuple2<Long, ArrayList<String>>>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String format(Tuple2<Long, ArrayList<String>> value) {
						String s = "{\"group\": " + value.f0 + ", \"member\": [";
						boolean first = true;
						for (String word : value.f1) {
							if (first) {
								first = false;
								s += "\"" + word + "\"";
							} else {
								s += ",\"" + word + "\"";
							}
						}
						s += "]}";
						return s;
					}
				});

		// sourceGroupSize.print();
		env.execute();
	}

}
