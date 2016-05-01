package de.bigdatapraktikum.twitternews.processing;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

/**
 * This class extracts all unique words for each tweet. A list of irrelevant
 * words can be used to exclude these words from the result set.
 */
public class EdgeMapper extends RichFlatMapFunction<Tuple2<Long, String>, Tuple3<String, String, Integer>> {
	
	@Override
	public void flatMap(Tuple2<Long, String> input,	Collector<Tuple3<String, String, Integer>> output) throws Exception {
			String[] words = input.f1.split(";");
			for (String w1 : words) {
				for (String w2 : words) {
					output.collect(new Tuple3<>(w1, w2, 1));
				}
			}
		
	}
}
