package de.bigdatapraktikum.twitternews.processing;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;


import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

/**
 * This class extracts all unique words for each tweet. A list of irrelevant words can be used to exclude these words from the result set.
 * 
 */

public class UniqueWordMapper extends RichFlatMapFunction<String, Tuple2<String, Integer>> {

	
	
	private static final long serialVersionUID = 7843218511470223386L;
	
	// set of irrelevant words
	private Set<String> irrelevantWords;
	
	private transient Set<String> emittedWords;
	
	public UniqueWordMapper() {
		this.irrelevantWords = new HashSet<>();
	}
	
	public UniqueWordMapper(String[] irrelevantWords) {
		// setup irrelevant words set
		this.irrelevantWords = new HashSet<>();
		Collections.addAll(this.irrelevantWords, irrelevantWords);
	}
	
	
	@Override
	public void open(Configuration parameters) throws Exception {
		// TODO Auto-generated method stub
		this.emittedWords = new HashSet<>();
	}
	@Override
	public void flatMap(String tweet, Collector<Tuple2<String, Integer>> output) throws Exception {
		// TODO Auto-generated method stub
		this.emittedWords.clear();
		StringTokenizer st = new StringTokenizer(tweet);
		
		while(st.hasMoreTokens()) {
			String word = st.nextToken().toLowerCase();
			
			if(!this.irrelevantWords.contains(word) && !this.emittedWords.contains(word)) {
				output.collect(new Tuple2<>(word,1));
				this.emittedWords.add(word);
			}
		}
	}

}
