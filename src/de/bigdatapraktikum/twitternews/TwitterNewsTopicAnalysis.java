package de.bigdatapraktikum.twitternews;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.operators.Order;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.tuple.Tuple2;

import de.bigdatapraktikum.twitternews.processing.IdfValueCalculator;
import de.bigdatapraktikum.twitternews.processing.UniqueWordMapper;
import de.bigdatapraktikum.twitternews.utils.AppConfig;


public class TwitterNewsTopicAnalysis {
	public static void main(String[] args) throws Exception {
		// set up the execution environment
		final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

		// get input data from previously stored twitter data
		DataSource<String> tweets = env.readTextFile(AppConfig.TWEET_STORAGE_PATH);
		// Calculates the number of tweets
		double amountOfTweets = tweets.count();
				
		// Calculates occurance for all the unique words. Excludes the irrelevant words that are defined in the AppConfig.java
		DataSet<Tuple2<String, Integer>> tweetFrequency = tweets.flatMap(new UniqueWordMapper(AppConfig.IRRELEVANT_WORDS)).groupBy(0).sum(1);
		
		
				
		// Prints all the Unique words with their occurance in descending order
		tweetFrequency.filter(new FilterFunction<Tuple2<String,Integer>>() {
			
			private static final long serialVersionUID = -3928683622587779482L;

			@Override
			public boolean filter(Tuple2<String, Integer> word) throws Exception {
				
				return word.f1 > 50;
			}
		}).sortPartition(1, Order.DESCENDING).print();
		
		// Calculates the IDF Values for all the words
		DataSet<Tuple2<String, Double>> idfValues = tweetFrequency.map(new IdfValueCalculator(amountOfTweets));
		
		// Prints all IDF Values
		idfValues.sortPartition(1, Order.DESCENDING).print();
		
		// run application
		env.execute();
	}
}
