package de.bigdatapraktikum.twitternews;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import de.bigdatapraktikum.twitternews.processing.IdfValueCalculator;
import de.bigdatapraktikum.twitternews.source.Tweet;
import de.bigdatapraktikum.twitternews.source.TwitterNewsSource;

public class TwitterNewsTopicAnalysis {
	public static void main(String[] args) throws Exception {

		// set up the execution environment
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		// get input data
		DataStreamSource<Tweet> tweets = env.addSource(new TwitterNewsSource());

		SingleOutputStreamOperator<Tuple2<Tweet, Double>> idfValues = tweets.flatMap(new IdfValueCalculator());

		// emit result
		idfValues.print();
		
		env.execute();
	}
}
