package de.bigdatapraktikum.twitternews;

import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.operators.FlatMapOperator;
import org.apache.flink.api.java.tuple.Tuple2;

import de.bigdatapraktikum.twitternews.processing.IdfValueCalculator;
import de.bigdatapraktikum.twitternews.utils.AppConfig;
import twitter4j.Status;

public class TwitterNewsTopicAnalysis {
	public static void main(String[] args) throws Exception {
		// set up the execution environment
		final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

		// get input data from previously stored twitter data
		DataSource<String> tweets = env.readTextFile(AppConfig.TWEET_STORAGE_PATH);

		// calculate idf values
		FlatMapOperator<String, Tuple2<Status, Double>> idfValues = tweets.flatMap(new IdfValueCalculator());

		// emit result
		idfValues.print();

		// run application
		env.execute();
	}
}
