package de.bigdatapraktikum.twitternews.output;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.io.TextOutputFormat.TextFormatter;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.core.fs.FileSystem.WriteMode;

import de.bigdatapraktikum.twitternews.config.AppConfig;

public class OutputNodes {

	public static void set(DataSet<Tuple2<String, Double>> filteredIdfValues) throws Exception {
		double maxIdfValue = filteredIdfValues.max(1).collect().get(0).f1;
		filteredIdfValues.writeAsFormattedText(AppConfig.RESOURCES_GRAPH_NODES, WriteMode.OVERWRITE,
				new TextFormatter<Tuple2<String, Double>>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String format(Tuple2<String, Double> value) {
						return "{\"data\":{\"id\":\"" + value.f0 + "\",\"name\":\"" + value.f0 + "\",\"score\":"
								+ (1 - value.f1 / maxIdfValue) + "},\"group\":\"nodes\"},";
					}
				}).setParallelism(1);
	}

}
