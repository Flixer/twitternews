package de.bigdatapraktikum.twitternews.processing;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;

public class IdMapper implements MapFunction<String, Tuple2<Long, String>> {
	private static final long serialVersionUID = 1L;

	// The constructor sets the amount of documents
	public IdMapper() {
			}

	@Override
	public Tuple2<Long, String> map(String input) throws Exception {
		// Calculates the IDF-Value
		String[] parts = input.split(";");
		return new Tuple2<>(Long.parseLong(parts[0]), parts[3]);
	}

}
