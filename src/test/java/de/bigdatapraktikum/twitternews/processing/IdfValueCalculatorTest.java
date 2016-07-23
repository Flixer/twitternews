package de.bigdatapraktikum.twitternews.processing;

import java.util.ArrayList;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.tuple.Tuple2;
import org.flinkspector.core.collection.ExpectedRecords;
import org.flinkspector.dataset.DataSetTestBase;
import org.junit.Test;

public class IdfValueCalculatorTest extends DataSetTestBase {

	@Test
	public void testIdfValueCalculator() throws Exception {
		ArrayList<Tuple2<String, Integer>> wordCountList = new ArrayList<>();

		wordCountList.add(new Tuple2<String, Integer>("A", 3));
		wordCountList.add(new Tuple2<String, Integer>("B", 2));
		wordCountList.add(new Tuple2<String, Integer>("C", 3));
		wordCountList.add(new Tuple2<String, Integer>("D", 1));

		// create DataSet from ArrayList
		DataSet<Tuple2<String, Integer>> wordCount = createTestDataSet(wordCountList);

		// execute EdgeMapper and get result
		DataSet<Tuple2<String, Double>> actual = wordCount.map(new IdfValueCalculator(4));

		// define expected result
		ArrayList<Tuple2<String, Double>> expectedArrayList = new ArrayList<>();
		expectedArrayList.add(new Tuple2<String, Double>("A", 0.12493873660829993));
		expectedArrayList.add(new Tuple2<String, Double>("B", 0.3010299956639812));
		expectedArrayList.add(new Tuple2<String, Double>("C", 0.12493873660829993));
		expectedArrayList.add(new Tuple2<String, Double>("D", 0.6020599913279624));

		ExpectedRecords<Tuple2<String, Double>> expected = new ExpectedRecords<Tuple2<String, Double>>()
				.expectAll(expectedArrayList);

		// assert actual and expected dataset
		assertDataSet(actual, expected);
	}

}
