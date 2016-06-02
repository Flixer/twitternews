package de.bigdatapraktikum.twitternews.processing;

import org.apache.flink.api.common.functions.MapFunction;

public class ChineseWhisperInitialClassMapper implements MapFunction<String, Integer> {
	private static final long serialVersionUID = 1L;

	public static int classId = 1;

	@Override
	public Integer map(String value) throws Exception {
		return classId++;
	}
}
