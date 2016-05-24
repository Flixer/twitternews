package de.bigdatapraktikum.twitternews.processing;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.tuple.Tuple3;
import org.flinkspector.core.collection.ExpectedRecords;
import org.flinkspector.dataset.DataSetTestBase;
import org.junit.Test;

import de.bigdatapraktikum.twitternews.source.Tweet;

public class UniqueWordMapperTest extends DataSetTestBase {

	@Test
	public void testUniqueWordMapper() throws Exception {
		String[] excludedWords = new String[] { "bb", "cc" };

		Tweet tweet1 = new Tweet(1, LocalDateTime.of(2016, 5, 20, 0, 0), "SOURCE", "AA BB CC", 1);
		Tweet tweet2 = new Tweet(2, LocalDateTime.of(2016, 5, 20, 0, 0), "SOURCE", "AA BB", 1);
		Tweet tweet3 = new Tweet(3, LocalDateTime.of(2016, 5, 20, 0, 0), "SOURCE", "CC", 1);
		Tweet tweet4 = new Tweet(4, LocalDateTime.of(2016, 5, 20, 0, 0), "SOURCE", "AA CC DD", 1);

		ArrayList<Tweet> tweetList = new ArrayList<>();
		tweetList.add(tweet1);
		tweetList.add(tweet2);
		tweetList.add(tweet3);
		tweetList.add(tweet4);

		// create DataSet from ArrayList
		DataSet<Tweet> tweets = createTestDataSet(tweetList);

		// execute EdgeMapper and get result
		DataSet<Tuple3<Tweet, String, Integer>> actual = tweets.flatMap(new UniqueWordMapper(excludedWords));

		// define expected result
		ArrayList<Tuple3<Tweet, String, Integer>> expectedArrayList = new ArrayList<>();
		expectedArrayList.add(new Tuple3<Tweet, String, Integer>(tweet1, "aa", 1));
		expectedArrayList.add(new Tuple3<Tweet, String, Integer>(tweet2, "aa", 1));
		expectedArrayList.add(new Tuple3<Tweet, String, Integer>(tweet4, "aa", 1));
		expectedArrayList.add(new Tuple3<Tweet, String, Integer>(tweet4, "dd", 1));

		ExpectedRecords<Tuple3<Tweet, String, Integer>> expected = new ExpectedRecords<Tuple3<Tweet, String, Integer>>()
				.expectAll(expectedArrayList);

		// assert actual and expected dataset
		assertDataSet(actual, expected);
	}

}
