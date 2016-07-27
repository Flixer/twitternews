package de.bigdatapraktikum.twitternews.output;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;

import org.apache.flink.api.common.operators.Order;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.tuple.Tuple2;

import de.bigdatapraktikum.twitternews.config.AppConfig;

public class OutputHistorical {

	public static void set(DataSet<Tuple2<String, HashMap<String, Integer>>> dateWithSourcesDistribution)
			throws Exception {
		File file = new File(AppConfig.RESOURCES_GRAPH_HISTORICAL);
		FileWriter fileWriter = new FileWriter(file, false);
		String fileContent = "";
		List<Tuple2<String, HashMap<String, Integer>>> values = dateWithSourcesDistribution
				.sortPartition(0, Order.ASCENDING).setParallelism(1).collect();
		boolean first = true;
		for (Tuple2<String, HashMap<String, Integer>> value : values) {
			if (first) {
				fileContent += "Date";
				for (String source : AppConfig.TWITTER_ACCOUNTS_READABLE) {
					fileContent += "," + source;
				}
				first = false;
			}

			fileContent += "\r\n" + value.f0;
			for (String source : AppConfig.TWITTER_ACCOUNTS_READABLE) {
				fileContent += "," + value.f1.getOrDefault(source, 0);
			}
		}
		fileWriter.write(fileContent);
		fileWriter.close();
	}

}
