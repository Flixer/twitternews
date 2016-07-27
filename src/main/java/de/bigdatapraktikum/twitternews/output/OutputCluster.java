package de.bigdatapraktikum.twitternews.output;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;

import de.bigdatapraktikum.twitternews.config.AppConfig;

public class OutputCluster {

	public static void set(
			DataSet<Tuple3<Long, ArrayList<String>, ArrayList<Tuple2<String, Long>>>> groupsWithWordsAndSources)
			throws Exception {

		File file = new File(AppConfig.RESOURCES_GRAPH_CLUSTER);
		FileWriter fileWriter = new FileWriter(file, false);
		String fileContent = "[";
		List<Tuple3<Long, ArrayList<String>, ArrayList<Tuple2<String, Long>>>> values = groupsWithWordsAndSources
				.collect();
		boolean first = true;
		for (Tuple3<Long, ArrayList<String>, ArrayList<Tuple2<String, Long>>> value : values) {
			if (!first) {
				fileContent += ",\r\n";
			} else {
				first = false;
			}
			fileContent += "{\"group\": " + value.f0 + ", \"words\": [";
			boolean firstWord = true;
			for (String word : value.f1) {
				if (firstWord) {
					firstWord = false;
				} else {
					fileContent += ", ";
				}
				fileContent += "\"" + word + "\"";
			}
			fileContent += "], \"sources\": [";
			boolean firstSource = true;
			for (Tuple2<String, Long> source : value.f2) {
				if (firstSource) {
					firstSource = false;
				} else {
					fileContent += ", ";
				}
				fileContent += "{\"name\": \"" + source.f0 + "\", \"count\": " + source.f1 + "}";
			}
			fileContent += "]}";
		}
		fileContent += "]";
		fileWriter.write(fileContent);
		fileWriter.close();
	}

}
