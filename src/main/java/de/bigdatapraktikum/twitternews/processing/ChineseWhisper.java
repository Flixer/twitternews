package de.bigdatapraktikum.twitternews.processing;

import java.util.HashMap;
import java.util.Map;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.graph.Edge;
import org.apache.flink.graph.Graph;
import org.apache.flink.graph.GraphAlgorithm;
import org.apache.flink.graph.Vertex;
import org.apache.flink.graph.spargel.MessageIterator;
import org.apache.flink.graph.spargel.MessagingFunction;
import org.apache.flink.graph.spargel.VertexUpdateFunction;

public class ChineseWhisper<K> implements GraphAlgorithm<K, Long, Double, Graph<K, Long, Double>> {

	private Integer maxIterations;

	public ChineseWhisper(Integer maxIterations) {
		this.maxIterations = maxIterations;
		// Graph<String, Integer, Integer> res =
		// graph.runScatterGatherIteration(new VertexGroupUpdater(),
		// new VertexGroupMessenger(), 10);
	}

	@Override
	public Graph<K, Long, Double> run(Graph<K, Long, Double> graph) throws Exception {

		return graph.runScatterGatherIteration(new VertexGroupUpdater<K>(), new VertexGroupMessenger<K>(),
				maxIterations);
	}

	// scatter: messaging
	public static final class VertexGroupMessenger<K> extends MessagingFunction<K, Long, Tuple2<Long, Double>, Double> {
		private static final long serialVersionUID = 1L;

		public void sendMessages(Vertex<K, Long> v) throws Exception {
			for (Edge<K, Double> edge : getEdges()) {
				sendMessageTo(edge.getTarget(), new Tuple2<>(v.f1, edge.getValue()));
			}
		}
	}

	// gather: vertex update

	public static final class VertexGroupUpdater<K> extends VertexUpdateFunction<K, Long, Tuple2<Long, Double>> {
		private static final long serialVersionUID = 1L;

		public void updateVertex(Vertex<K, Long> v, MessageIterator<Tuple2<Long, Double>> m) throws Exception {
			Map<Long, Double> receivedGroupsWithScores = new HashMap<Long, Double>();

			for (Tuple2<Long, Double> message : m) {
				Long group = message.f0;
				Double score = message.f1;

				// if group was already received
				if (receivedGroupsWithScores.containsKey(group)) {
					Double newScore = score + receivedGroupsWithScores.get(group);
					receivedGroupsWithScores.put(group, newScore);

				} else {
					/// if it is the first message with the group
					receivedGroupsWithScores.put(group, score);
				}
			}
			if (!receivedGroupsWithScores.isEmpty()) {
				Double maxValue = 0.0;
				Long maxGroup = (long) 0;
				for (Map.Entry<Long, Double> entry : receivedGroupsWithScores.entrySet()) {
					if (entry.getValue() > maxValue) {
						maxValue = entry.getValue();
						maxGroup = entry.getKey();
					}
				}

				setNewVertexValue(maxGroup);
			}
		}
	}

}
