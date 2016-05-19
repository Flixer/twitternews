package de.bigdatapraktikum.twitternews;

import org.apache.flink.api.common.functions.ReduceFunction;
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
		DataSet<Tuple2<Long, String>> filteredWordsInTweets = twitterNewsTopicAnalysis.getFilteredWordsInTweets();
		
		//TODO: Rethink the variable naming
		
		DataSet<Tuple2<Long, String>> aggregatedWordsinTweets = filteredWordsInTweets.groupBy(0)
				.reduce(new ReduceFunction<Tuple2<Long, String>>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Tuple2<Long, String> reduce(Tuple2<Long, String> in1, Tuple2<Long, String> in2)
							throws Exception {

						return new Tuple2<>(in1.f0, in1.f1 + ";" + in2.f1);
					}
				});
		// create the graph
		
		// TODO: Write own Grouper, String Order should be indiffernt
		
		DataSet<Tuple3<String, String, Integer>> edges = aggregatedWordsinTweets.flatMap(new EdgeMapper()).groupBy(0, 1)
				.sum(2);
		Graph<String, NullValue, Integer> graph = Graph.fromTupleDataSet(edges, env);
		System.out.println();

		System.out.println(graph.toString());
		// return graph
		env.execute();
	}

	// run application
	// env.execute();
}
