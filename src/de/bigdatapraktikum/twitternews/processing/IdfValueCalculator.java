package de.bigdatapraktikum.twitternews.processing;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;

import de.bigdatapraktikum.twitternews.source.Tweet;

public class IdfValueCalculator implements FlatMapFunction<Tweet, Tuple2<Tweet, Double>> {
	private static final long serialVersionUID = 1L;

	@Override
	public void flatMap(Tweet tweet, Collector<Tuple2<Tweet, Double>> collector) throws Exception {
		// TODO Implement IDF algorithm
		collector.collect(new Tuple2<Tweet, Double>(tweet, 12.34));
	}

}
