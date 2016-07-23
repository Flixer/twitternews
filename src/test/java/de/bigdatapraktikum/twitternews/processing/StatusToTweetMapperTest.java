package de.bigdatapraktikum.twitternews.processing;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.apache.flink.api.java.DataSet;
import org.flinkspector.core.collection.ExpectedRecords;
import org.flinkspector.dataset.DataSetTestBase;
import org.junit.Test;

import de.bigdatapraktikum.twitternews.source.Tweet;
import de.bigdatapraktikum.twitternews.utils.StatusTestClass;
import twitter4j.Status;

public class StatusToTweetMapperTest extends DataSetTestBase {

	@Test
	public void testStatusToTweetMapper() throws Exception {
		Tweet tweet1 = new Tweet(1, LocalDateTime.of(2016, 5, 20, 0, 0), "SOURCE", "A B C", 1);
		Tweet tweet2 = new Tweet(2, LocalDateTime.of(2016, 5, 20, 0, 0), "SOURCE", "A B", 1);
		Tweet tweet3 = new Tweet(3, LocalDateTime.of(2016, 5, 20, 0, 0), "SOURCE", "C", 1);
		Tweet tweet4 = new Tweet(4, LocalDateTime.of(2016, 5, 20, 0, 0), "SOURCE", "A C D", 1);

		ArrayList<Status> wordCountList = new ArrayList<>();
		wordCountList.add(new StatusTestClass(tweet1));
		wordCountList.add(new StatusTestClass(tweet2));
		wordCountList.add(new StatusTestClass(tweet3));
		wordCountList.add(new StatusTestClass(tweet4));

		// create DataSet from ArrayList
		DataSet<Status> tweetStatuses = createTestDataSet(wordCountList);

		// execute EdgeMapper and get result
		DataSet<Tweet> actual = tweetStatuses.map(new StatusToTweetMapper());

		// define expected result
		ArrayList<Tweet> expectedArrayList = new ArrayList<>();
		expectedArrayList.add(tweet1);
		expectedArrayList.add(tweet2);
		expectedArrayList.add(tweet3);
		expectedArrayList.add(tweet4);

		ExpectedRecords<Tweet> expected = new ExpectedRecords<Tweet>().expectAll(expectedArrayList);

		// assert actual and expected dataset
		assertDataSet(actual, expected);
	}

}
