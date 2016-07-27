package de.bigdatapraktikum.twitternews.output;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.tuple.Tuple2;

import de.bigdatapraktikum.twitternews.config.AppConfig;

public class OutputNodes {

	public static void set(DataSet<Tuple2<String, Double>> filteredIdfValues, String ext) throws Exception {
		double maxIdfValue = filteredIdfValues.max(1).collect().get(0).f1;

		File file = new File(AppConfig.RESOURCES_GRAPH_NODES + ext);
		FileWriter fileWriter = new FileWriter(file, false);
		String fileContent = "[";
		List<Tuple2<String, Double>> values = filteredIdfValues.collect();
		boolean first = true;
		for (Tuple2<String, Double> value : values) {
			if (!first) {
				fileContent += ",\r\n";
			} else {
				first = false;
			}
			fileContent += "{\"data\":{\"id\":\"" + value.f0 + "\",\"name\":\"" + value.f0 + "\",\"score\":"
					+ (1 - value.f1 / maxIdfValue) + "},\"group\":\"nodes\"}";
		}
		fileContent += "]";
		fileWriter.write(fileContent);
		fileWriter.close();
	}

}
