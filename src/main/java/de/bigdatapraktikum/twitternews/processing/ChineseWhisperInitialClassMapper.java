package de.bigdatapraktikum.twitternews.processing;

import org.apache.flink.api.common.functions.MapFunction;

public class ChineseWhisperInitialClassMapper implements MapFunction<String, Long> {
	private static final long serialVersionUID = 1L;

	public static long classId = 1;

	@Override
	public Long map(String value) throws Exception {
		return classId++;
	}
}
