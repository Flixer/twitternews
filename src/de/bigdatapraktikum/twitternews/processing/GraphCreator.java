package de.bigdatapraktikum.twitternews.processing;

import java.util.ArrayList;
import java.util.List;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.graph.Edge;
import org.apache.flink.graph.Graph;
import org.apache.flink.graph.Vertex;

import de.bigdatapraktikum.twitternews.utils.AppConfig;

import org.apache.flink.api.java.operators.DataSource;

public class GraphCreator {
	public static void main(String[] args) throws Exception {
		final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
				
		Vertex<Long, String> a = new Vertex<Long, String>(1L, "foo");
		Vertex<Long, String> b = new Vertex<Long, String>(2L, "bar");
		Vertex<Long, String> c = new Vertex<Long, String>(3L, "foo");
		
		List<Vertex<Long, String>> vertextList = new ArrayList<Vertex<Long, String>>();
		
		vertextList.add(a);
		vertextList.add(b);
		vertextList.add(c);
		
		Edge<Long, Double> d = new Edge<Long, Double>(1L, 2L, 0.5);
		Edge<Long, Double> e = new Edge<Long, Double>(1L, 3L, 0.5);
		
		List<Edge<Long, Double>> edgeList = new ArrayList<Edge<Long, Double>>();
		
		edgeList.add(d);
		edgeList.add(e);
		
		DataSet<Vertex<Long, String>> vertices = (DataSet<Vertex<Long, String>>) vertextList.iterator();
		DataSet<Edge<Long, Double>> edges = (DataSet<Edge<Long, Double>>) edgeList.iterator();
				
		Graph<Long, String, Double> graph = Graph.fromDataSet(vertices, edges, env);
		
		env.execute();
		
	}
}