package de.bigdatapraktikum.twitternews.processing;

import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;

import de.bigdatapraktikum.twitternews.source.Tweet;

public class UniqueWordsIdfJoin
		implements JoinFunction<Tuple3<Tweet, String, Integer>, Tuple2<String, Double>, Tuple2<Tweet, String>> {
	private static final long serialVersionUID = 1L;

	@Override
	public Tuple2<Tweet, String> join(Tuple3<Tweet, String, Integer> uniqueWords, Tuple2<String, Double> idfWords) {
		// multiply the points and rating and construct a new output tuple

		return new Tuple2<Tweet, String>(uniqueWords.f0, uniqueWords.f1);
	}
}
