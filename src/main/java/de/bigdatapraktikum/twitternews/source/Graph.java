package de.bigdatapraktikum.twitternews.source;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.graph.Edge;
import org.apache.flink.graph.Vertex;

public class Graph {

	// a Graph is represented by a DataSet of vertices and a DataSet of edges
	private DataSet<Vertex<Long, String>> vertices;
	private DataSet<Edge<Long, String>> edges;

	public Graph(DataSet<Vertex<Long, String>> vertices, DataSet<Edge<Long, String>> edges) {
		this.vertices = vertices;
		this.edges = edges;
	}

	public DataSet<Vertex<Long, String>> getVertices() {
		return vertices;
	}

	public void setVertices(DataSet<Vertex<Long, String>> vertices) {
		this.vertices = vertices;
	}

	public DataSet<Edge<Long, String>> getEdges() {
		return edges;
	}

	public void setEdges(DataSet<Edge<Long, String>> edges) {
		this.edges = edges;
	}
}
