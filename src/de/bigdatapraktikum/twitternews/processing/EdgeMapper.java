package de.bigdatapraktikum.twitternews.processing;

import java.util.ArrayList;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.util.Collector;

import de.bigdatapraktikum.twitternews.source.Tweet;

/**
 * This class extracts all unique words for each tweet. A list of irrelevant
 * words can be used to exclude these words from the result set.
 */
public class EdgeMapper extends RichFlatMapFunction<Tuple2<Tweet, ArrayList<String>>, Tuple3<String, String, Integer>> {
	private static final long serialVersionUID = 1L;

	@Override
	public void flatMap(Tuple2<Tweet, ArrayList<String>> input, Collector<Tuple3<String, String, Integer>> output)
			throws Exception {
		ArrayList<String> words = input.f1;

		// combinatorics without order without putting elements back
		// insert Matrix (I = Insert, B = break, x = not processed)
		// B x x x x
		// I B x x x
		// I I B x x
		// I I I B x
		// I I I I B
		for (String w1 : words) {
			for (String w2 : words) {
				if (w2.equals(w1)) {
					break;
				}
				// always add lexicographically smaller string first
				if (w1.compareTo(w2) < 0) {
					output.collect(new Tuple3<>(w1, w2, 1));
				} else {
					output.collect(new Tuple3<>(w2, w1, 1));
				}
			}
		}

	}
}
