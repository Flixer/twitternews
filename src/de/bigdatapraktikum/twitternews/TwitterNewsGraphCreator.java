package de.bigdatapraktikum.twitternews;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.flink.api.common.operators.Order;
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
import org.apache.flink.graph.spargel.MessageIterator;
import org.apache.flink.graph.spargel.MessagingFunction;
import org.apache.flink.graph.spargel.VertexUpdateFunction;

import de.bigdatapraktikum.twitternews.processing.ChineseWhisperInitialClassMapper;
import de.bigdatapraktikum.twitternews.processing.EdgeMapper;
import de.bigdatapraktikum.twitternews.processing.TweetFilter;
import de.bigdatapraktikum.twitternews.source.Tweet;
import de.bigdatapraktikum.twitternews.utils.AppConfig;

// this class creates a co-occurrence graph
public class TwitterNewsGraphCreator {
	private static Graph<String, Integer, Integer> graph;
	private static List<Vertex<String, Integer>> verticleList;

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
		graph = Graph.fromTupleDataSet(edges, new ChineseWhisperInitialClassMapper(), env);

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

		verticleList = graph.getVertices().collect();
		graph.getVertices().print();
		Graph<String, Integer, Integer> res = graph.runScatterGatherIteration(new VertexGroupUpdater(),
				new VertexGroupMessenger(), 1);
		res.getVertices().print();

		env.execute();
	}

	// scatter: messaging
	public static final class VertexGroupMessenger extends MessagingFunction<String, Integer, Integer, Integer> {
		private static final long serialVersionUID = 1L;

		public void sendMessages(Vertex<String, Integer> v) throws Exception {
			HashMap<Integer, Integer> groupToEdgesWeightSum = new HashMap<>();
			Integer maxWeightGroup = null;
			for (Edge<String, Integer> edge : getEdges()) {
				String edgeTarget = edge.getTarget();
				if (edgeTarget.equals(v.getId())) {
					edgeTarget = edge.getSource();
				}

				for (Vertex<String, Integer> verticle : verticleList) {
					if (verticle.getId().equals(edgeTarget)) {
						int groupId = verticle.getValue();
						int edgeWeightSum = edge.getValue();
						if (groupToEdgesWeightSum.containsKey(groupId)) {
							edgeWeightSum += groupToEdgesWeightSum.get(groupId);
						}
						groupToEdgesWeightSum.put(groupId, edgeWeightSum);
						if (maxWeightGroup == null || groupToEdgesWeightSum.get(maxWeightGroup) < edgeWeightSum) {
							maxWeightGroup = groupId;
						}
						break;
					}
				}
			}
			sendMessageTo(v.getId(), new Integer(maxWeightGroup != null ? maxWeightGroup : v.getValue()));
		}
	}

	// gather: vertex update
	public static final class VertexGroupUpdater extends VertexUpdateFunction<String, Integer, Integer> {
		private static final long serialVersionUID = 1L;

		public void updateVertex(Vertex<String, Integer> v, MessageIterator<Integer> m) throws Exception {
			int groupId = v.getValue();
			for (int mGroupId : m) {
				groupId = mGroupId;
			}
			setNewVertexValue(groupId);
		}
	}
}
