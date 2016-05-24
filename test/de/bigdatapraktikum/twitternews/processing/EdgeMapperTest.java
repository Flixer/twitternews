package de.bigdatapraktikum.twitternews.processing;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.operators.FlatMapOperator;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.flinkspector.core.collection.ExpectedRecords;
import org.flinkspector.dataset.DataSetTestBase;
import org.junit.Test;

import de.bigdatapraktikum.twitternews.source.Tweet;

public class EdgeMapperTest extends DataSetTestBase {

	@Test
	public void testEdgeMapper() throws Exception {
		ArrayList<Tuple2<Tweet, ArrayList<String>>> tweetList = new ArrayList<>();

		// Tweet 1
		ArrayList<String> tweet1Words = new ArrayList<String>();
		tweet1Words.add("A");
		tweet1Words.add("B");
		tweet1Words.add("C");
		tweetList.add(new Tuple2<Tweet, ArrayList<String>>(
				new Tweet(1, LocalDateTime.of(2016, 5, 20, 0, 0), "SOURCE", "A B C", 1), tweet1Words));

		// Tweet 2
		ArrayList<String> tweet2Words = new ArrayList<String>();
		tweet2Words.add("A");
		tweet2Words.add("B");
		tweetList.add(new Tuple2<Tweet, ArrayList<String>>(
				new Tweet(2, LocalDateTime.of(2016, 5, 20, 0, 0), "SOURCE", "A B", 1), tweet2Words));

		// Tweet 3
		ArrayList<String> tweet3Words = new ArrayList<String>();
		tweet3Words.add("C");
		tweetList.add(new Tuple2<Tweet, ArrayList<String>>(
				new Tweet(3, LocalDateTime.of(2016, 5, 20, 0, 0), "SOURCE", "C", 1), tweet3Words));

		// Tweet 4
		ArrayList<String> tweet4Words = new ArrayList<String>();
		tweet4Words.add("A");
		tweet4Words.add("C");
		tweet4Words.add("D");
		tweetList.add(new Tuple2<Tweet, ArrayList<String>>(
				new Tweet(4, LocalDateTime.of(2016, 5, 20, 0, 0), "SOURCE", "A C D", 1), tweet4Words));

		// create DataSet from ArrayList
		DataSet<Tuple2<Tweet, ArrayList<String>>> tweetListDataSet = createTestDataSet(tweetList);

		// execute EdgeMapper and get result
		FlatMapOperator<Tuple2<Tweet, ArrayList<String>>, Tuple3<String, String, Integer>> actual = tweetListDataSet
				.flatMap(new EdgeMapper());

		// define expected result
		ArrayList<Tuple3<String, String, Integer>> expectedArrayList = new ArrayList<>();
		expectedArrayList.add(new Tuple3<String, String, Integer>("A", "B", 1));
		expectedArrayList.add(new Tuple3<String, String, Integer>("A", "C", 1));
		expectedArrayList.add(new Tuple3<String, String, Integer>("B", "C", 1));
		expectedArrayList.add(new Tuple3<String, String, Integer>("A", "B", 1));
		expectedArrayList.add(new Tuple3<String, String, Integer>("A", "C", 1));
		expectedArrayList.add(new Tuple3<String, String, Integer>("A", "D", 1));
		expectedArrayList.add(new Tuple3<String, String, Integer>("C", "D", 1));
		ExpectedRecords<Tuple3<String, String, Integer>> expected = new ExpectedRecords<Tuple3<String, String, Integer>>()
				.expectAll(expectedArrayList);

		// assert actual and expected dataset
		assertDataSet(actual, expected);
	}

}
