package de.bigdatapraktikum.twitternews.processing;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;

import twitter4j.Status;

public class IdfValueCalculator implements FlatMapFunction<String, Tuple2<Status, Double>> {
	private static final long serialVersionUID = 1L;

	@Override
	public void flatMap(String tweetString, Collector<Tuple2<Status, Double>> collector) throws Exception {
		// TODO Implement IDF algorithm
		System.out.println("--> " + tweetString + " <--");
//		collector.collect(new Tuple2<Status, Double>(tweet, 12.34));
	}

}
