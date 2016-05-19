package de.bigdatapraktikum.twitternews;

import java.util.ArrayList;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.TextOutputFormat.TextFormatter;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.core.fs.FileSystem.WriteMode;
import org.apache.flink.graph.Edge;
import org.apache.flink.graph.Graph;
import org.apache.flink.types.NullValue;

import de.bigdatapraktikum.twitternews.processing.EdgeMapper;
import de.bigdatapraktikum.twitternews.source.Tweet;
import de.bigdatapraktikum.twitternews.utils.AppConfig;

// this class creates a co-occurrence graph
public class TwitterNewsGraphCreator {
	public static void main(String[] args) throws Exception {
		final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		env.setParallelism(1);

		// returns the co-occurrence graph with the help of the
		// TwitterNewsTopicAnalysis
		// public Graph<String, NullValue, Integer> getCoOccurrenceGraph()
		// throws Exception{

		// get the filtered tweets
		TwitterNewsTopicAnalysis twitterNewsTopicAnalysis = new TwitterNewsTopicAnalysis();
		DataSet<Tuple2<Tweet, ArrayList<String>>> wordsPerTweet = twitterNewsTopicAnalysis
				.getFilteredWordsInTweets(env);

		// create the graph
		DataSet<Tuple3<String, String, Integer>> edges = wordsPerTweet.flatMap(new EdgeMapper()).groupBy(0, 1).sum(2);
		Graph<String, NullValue, Integer> graph = Graph.fromTupleDataSet(edges, env);
		// graph.getEdges().print();
		// TODO add data sink for graph

		graph.getEdges().writeAsFormattedText("resources/edges.txt", WriteMode.OVERWRITE,
				new TextFormatter<Edge<String, Integer>>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String format(Edge<String, Integer> value) {
						float weight = Math.min((float) value.f2 / 15, 1);
						int colorIntensity = (int) (200. * (1. - weight));
						return "{\"data\":{\"source\":\"" + value.f0 + "\",\"target\":\"" + value.f1 + "\",\"weight\":"
								+ weight + "},\"group\":\"edges\",\"style\":{\"line-color\":\"rgb(200, "
								+ colorIntensity + "," + colorIntensity / 2 + ")\"}},";
					}
				});

		env.execute();
	}
}
