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
 * This class extracts all unique words for each tweet. A list of irrelevant
 * words can be used to exclude these words from the result set.
 */
public class UniqueWordMapper extends RichFlatMapFunction<String, Tuple2<String, Integer>> {
	private static final long serialVersionUID = 1L;

	// set of irrelevant words
	private Set<String> irrelevantWords;

	// TODO describe what this set is used for (i currently do not completely
	// understand, why this set is used)
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
		this.emittedWords = new HashSet<>();
	}

	@Override
	public void flatMap(String tweet, Collector<Tuple2<String, Integer>> output) throws Exception {
		// TODO filter words even more:
		// 1. add words to AppConfig.IRRELEVANT_WORDS
		// 2. prevent dates, numbers and maybe urls from being collected
		// 3. remove punctuation like .,?!;-"'(), maybe delete everythink which
		// is a non word character (care since .replaceAll("\W", "") will also
		// remove הצü)
		this.emittedWords.clear();
		StringTokenizer st = new StringTokenizer(tweet);

		while (st.hasMoreTokens()) {
			String word = st.nextToken().toLowerCase();

			if (!this.irrelevantWords.contains(word) && !this.emittedWords.contains(word)) {
				output.collect(new Tuple2<>(word, 1));
				this.emittedWords.add(word);
			}
		}
	}
}
