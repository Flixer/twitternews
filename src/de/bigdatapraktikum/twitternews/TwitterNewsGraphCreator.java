package de.bigdatapraktikum.twitternews;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.operators.Order;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.io.TextOutputFormat.TextFormatter;
import org.apache.flink.api.java.operators.SortPartitionOperator;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.core.fs.FileSystem.WriteMode;
import org.apache.flink.graph.Edge;
import org.apache.flink.graph.Graph;
import org.apache.flink.graph.Vertex;
import org.apache.flink.graph.spargel.MessageIterator;
import org.apache.flink.graph.spargel.MessagingFunction;
import org.apache.flink.graph.spargel.VertexUpdateFunction;
import org.apache.flink.types.NullValue;

import de.bigdatapraktikum.twitternews.processing.ChineseWhisperInitialClassMapper;
import de.bigdatapraktikum.twitternews.processing.EdgeMapper;
import de.bigdatapraktikum.twitternews.processing.TweetFilter;
import de.bigdatapraktikum.twitternews.source.Tweet;
import de.bigdatapraktikum.twitternews.utils.AppConfig;
import scala.collection.mutable.HashMap;

// this class creates a co-occurrence graph
public class TwitterNewsGraphCreator {
	public static void main(String[] args) throws Exception {
		TweetFilter tweetFilter = new TweetFilter();
		// tweetFilter.setDateFrom(LocalDateTime.now().minusDays(7));
		// tweetFilter.setDateTo(LocalDateTime.now().minusHours(0));

		final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		env.setParallelism(1);

		// returns the co-occurrence graph with the help of the
		// TwitterNewsTopicAnalysis
		// public Graph<String, NullValue, Integer> getCoOccurrenceGraph()
		// throws Exception{

		// get the filtered tweets
		TwitterNewsTopicAnalysis twitterNewsTopicAnalysis = new TwitterNewsTopicAnalysis();
		DataSet<Tuple2<Tweet, ArrayList<String>>> wordsPerTweet = twitterNewsTopicAnalysis.getFilteredWordsInTweets(env,
				tweetFilter);

		// create the graph
		DataSet<Tuple3<String, String, Integer>> edges = wordsPerTweet.flatMap(new EdgeMapper()).groupBy(0, 1).sum(2);
		Graph<String, Integer, Integer> graph = Graph.fromTupleDataSet(edges, new ChineseWhisperInitialClassMapper(),
				env);

		// get the strongest connection between two nodes
		int maxEdgeCount = graph.getEdges().max(2).collect().get(0).f2;
		graph.getEdges().writeAsFormattedText(AppConfig.RESOURCES_GRAPH_EDGES, WriteMode.OVERWRITE,
				new TextFormatter<Edge<String, Integer>>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String format(Edge<String, Integer> value) {
						float weight = Math.min((float) value.f2 / maxEdgeCount, 1);
						int colorIntensityR = (int) (180. + (75 * weight));
						int colorIntensityG = (int) (180. * (1. - weight));
						int colorIntensityB = (int) (110. * (1. - weight));
						return "{\"data\":{\"source\":\"" + value.f0 + "\",\"target\":\"" + value.f1 + "\",\"weight\":"
								+ weight + "},\"group\":\"edges\",\"style\":{\"line-color\":\"rgb(" + colorIntensityR
								+ ", " + colorIntensityG + "," + colorIntensityB + ")\"}},";
					}
				});

		// TODO: randomize order of verticles
		DataSet<Vertex<String, Integer>> v = graph.getVertices()
				.sortPartition(new KeySelector<Vertex<String, Integer>, Integer>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Integer getKey(Vertex<String, Integer> value) throws Exception {
						return (int) (Math.random() * 1000);
					}
				}, Order.ASCENDING);
		graph.runScatterGatherIteration(new VertexUpdateFunction<String, Integer, Integer>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void updateVertex(Vertex<String, Integer> v, MessageIterator<Integer> m) throws Exception {
				v.setValue(m.next().intValue());
			}
		}, new MessagingFunction<String, Integer, Integer, Integer>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void sendMessages(Vertex<String, Integer> v) throws Exception {
				HashMap<Integer, Integer> groupToEdgesWeightSum = new HashMap<>();
				for (Edge<String, Integer> edge : getEdges()) {
					// TODO: get vertex object of edge target, update current
					// vertex class with the class of the heighest edge weight
					// sum to the current vertex
				}
			}
		}, 1);

		env.execute();
	}
}
