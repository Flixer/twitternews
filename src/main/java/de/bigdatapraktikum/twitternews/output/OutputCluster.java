package de.bigdatapraktikum.twitternews.output;

import java.util.ArrayList;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.io.TextOutputFormat.TextFormatter;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.core.fs.FileSystem.WriteMode;

import de.bigdatapraktikum.twitternews.config.AppConfig;

public class OutputCluster {

	public static void set(
			DataSet<Tuple3<Long, ArrayList<String>, ArrayList<Tuple2<String, Long>>>> groupsWithWordsAndSources) {
		groupsWithWordsAndSources.writeAsFormattedText(AppConfig.RESOURCES_GRAPH_CLUSTER, WriteMode.OVERWRITE,
				new TextFormatter<Tuple3<Long, ArrayList<String>, ArrayList<Tuple2<String, Long>>>>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String format(Tuple3<Long, ArrayList<String>, ArrayList<Tuple2<String, Long>>> value) {
						String s = "{\"group\": " + value.f0 + ", \"words\": [";
						boolean first = true;
						for (String word : value.f1) {
							if (first) {
								first = false;
							} else {
								s += ", ";
							}
							s += "\"" + word + "\"";
						}
						s += "], \"sources\": [";
						first = true;
						for (Tuple2<String, Long> source : value.f2) {
							if (first) {
								first = false;
							} else {
								s += ", ";
							}
							s += "{\"name\": \"" + source.f0 + "\", \"count\": " + source.f1 + "}";
						}
						s += "]}, ";
						return s;
					}
				}).setParallelism(1);
	}

}
