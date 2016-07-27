package de.bigdatapraktikum.twitternews.output;

import java.util.HashMap;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.io.TextOutputFormat.TextFormatter;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.core.fs.FileSystem.WriteMode;

import de.bigdatapraktikum.twitternews.config.AppConfig;

public class OutputHistorical {

	public static void set(DataSet<Tuple2<String, HashMap<String, Integer>>> dateWithSourcesDistribution)
			throws Exception {
		String firstDate = dateWithSourcesDistribution.collect().get(0).f0;
		dateWithSourcesDistribution.writeAsFormattedText(AppConfig.RESOURCES_GRAPH_HISTORICAL, WriteMode.OVERWRITE,
				new TextFormatter<Tuple2<String, HashMap<String, Integer>>>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String format(Tuple2<String, HashMap<String, Integer>> value) {
						String s = "";
						if (firstDate.equals(value.f0)) {
							s += "Date";
							for (String source : AppConfig.TWITTER_ACCOUNTS_READABLE) {
								s += "," + source;
							}
							s += "\r\n";
						}

						s += value.f0;
						for (String source : AppConfig.TWITTER_ACCOUNTS_READABLE) {
							s += "," + value.f1.getOrDefault(source, 0);
						}
						return s;
					}
				}).setParallelism(1);
	}

}
