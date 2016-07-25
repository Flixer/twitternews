package de.bigdatapraktikum.twitternews.processing;

import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.graph.Vertex;

import de.bigdatapraktikum.twitternews.source.Tweet;

public class TweetGroupJoin
		implements JoinFunction<Tuple2<Tweet, String>, Vertex<String, Long>, Tuple3<Long, String, Long>> {
	private static final long serialVersionUID = 1L;

	@Override
	public Tuple3<Long, String, Long> join(Tuple2<Tweet, String> first, Vertex<String, Long> second) throws Exception {

		return new Tuple3<Long, String, Long>(first.f0.getId(), first.f0.getSource(), second.f1);
	}

}
