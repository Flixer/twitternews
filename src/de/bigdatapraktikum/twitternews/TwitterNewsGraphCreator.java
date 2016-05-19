package de.bigdatapraktikum.twitternews;

import java.util.ArrayList;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.graph.Graph;
import org.apache.flink.types.NullValue;

import de.bigdatapraktikum.twitternews.processing.EdgeMapper;

// this class creates a co-occurrence graph
public class TwitterNewsGraphCreator {
	public static void main(String[] args) throws Exception {
		final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

		// returns the co-occurrence graph with the help of the
		// TwitterNewsTopicAnalysis
		// public Graph<String, NullValue, Integer> getCoOccurrenceGraph()
		// throws Exception{

		// get the filtered tweets
		TwitterNewsTopicAnalysis twitterNewsTopicAnalysis = new TwitterNewsTopicAnalysis();
		DataSet<Tuple2<Long, ArrayList<String>>> wordsPerTweet = twitterNewsTopicAnalysis.getFilteredWordsInTweets();

		// create the graph
		// TODO: Write own Grouper, String Order should be indifferent
		DataSet<Tuple3<String, String, Integer>> edges = wordsPerTweet.flatMap(new EdgeMapper()).groupBy(0, 1).sum(2);
		Graph<String, NullValue, Integer> graph = Graph.fromTupleDataSet(edges, env);

		// TODO add data sink for graph

		env.execute();
	}
}
