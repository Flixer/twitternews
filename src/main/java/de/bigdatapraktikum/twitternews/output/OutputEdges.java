package de.bigdatapraktikum.twitternews.output;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.io.TextOutputFormat.TextFormatter;
import org.apache.flink.core.fs.FileSystem.WriteMode;
import org.apache.flink.graph.Edge;

import de.bigdatapraktikum.twitternews.config.AppConfig;

public class OutputEdges {

	public static void set(DataSet<Edge<String, Double>> edges) throws Exception {
		// get the strongest connection between two nodes
		double maxEdgeCount = edges.max(2).collect().get(0).f2;

		edges.writeAsFormattedText(AppConfig.RESOURCES_GRAPH_EDGES, WriteMode.OVERWRITE,
				new TextFormatter<Edge<String, Double>>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String format(Edge<String, Double> value) {
						double weight = Math.min(value.f2 / maxEdgeCount, 1);
						int colorIntensityR = (int) (180. + (75 * weight));
						int colorIntensityG = (int) (180. * (1. - weight));
						int colorIntensityB = (int) (110. * (1. - weight));
						return "{\"data\":{\"source\":\"" + value.f0 + "\",\"target\":\"" + value.f1 + "\",\"weight\":"
								+ weight + "},\"group\":\"edges\",\"style\":{\"line-color\":\"rgb(" + colorIntensityR
								+ ", " + colorIntensityG + "," + colorIntensityB + ")\"}},";
					}
				}).setParallelism(1);
	}

}
