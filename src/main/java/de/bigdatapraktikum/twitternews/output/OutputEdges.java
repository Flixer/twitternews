package de.bigdatapraktikum.twitternews.output;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.graph.Edge;

import de.bigdatapraktikum.twitternews.config.AppConfig;

public class OutputEdges {

	public static void set(DataSet<Edge<String, Double>> edges) throws Exception {
		// get the strongest connection between two nodes
		double maxEdgeCount = edges.max(2).collect().get(0).f2;

		File file = new File(AppConfig.RESOURCES_GRAPH_EDGES);
		FileWriter fileWriter = new FileWriter(file, false);
		String fileContent = "[";
		List<Edge<String, Double>> values = edges.collect();
		boolean first = true;
		for (Edge<String, Double> value : values) {
			double weight = Math.min(value.f2 / maxEdgeCount, 1);
			int colorIntensityR = (int) (180. + (75 * weight));
			int colorIntensityG = (int) (180. * (1. - weight));
			int colorIntensityB = (int) (110. * (1. - weight));
			if (!first) {
				fileContent += ",\r\n";
			} else {
				first = false;
			}
			fileContent += "{\"data\":{\"source\":\"" + value.f0 + "\",\"target\":\"" + value.f1 + "\",\"weight\":"
					+ weight + "},\"group\":\"edges\",\"style\":{\"line-color\":\"rgb(" + colorIntensityR + ", "
					+ colorIntensityG + "," + colorIntensityB + ")\"}}";
		}
		fileContent += "]";
		fileWriter.write(fileContent);
		fileWriter.close();
	}

}
