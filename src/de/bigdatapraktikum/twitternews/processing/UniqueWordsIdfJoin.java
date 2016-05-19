package de.bigdatapraktikum.twitternews.processing;

import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;

public class UniqueWordsIdfJoin
		implements JoinFunction<Tuple3<Long, String, Integer>, Tuple2<String, Double>, Tuple2<Long, String>> {
	private static final long serialVersionUID = 1L;

	@Override
	public Tuple2<Long, String> join(Tuple3<Long, String, Integer> uniqueWords, Tuple2<String, Double> idfWords) {
		// multiply the points and rating and construct a new output tuple

		return new Tuple2<Long, String>(uniqueWords.f0, uniqueWords.f1);
	}
}
