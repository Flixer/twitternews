package de.bigdatapraktikum.twitternews.processing;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

import de.bigdatapraktikum.twitternews.source.Tweet;

/**
 * This class extracts all unique words for each tweet. A list of irrelevant
 * words can be used to exclude these words from the result set.
 */
public class UniqueWordMapper extends RichFlatMapFunction<Tweet, Tuple3<Tweet, String, Integer>> {
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
	public void flatMap(Tweet tweet, Collector<Tuple3<Tweet, String, Integer>> output) throws Exception {
		// TODO filter words even more:
		// - add words to AppConfig.IRRELEVANT_WORDS

		this.emittedWords.clear();
		StringTokenizer st = new StringTokenizer(tweet.getContent());

		while (st.hasMoreTokens()) {
			String word = st.nextToken().toLowerCase().trim();
			word = word.replaceAll("[^a-zA-ZäöüÄÖÜß]", "");
			boolean isNumber = word.matches("^[0-9]+$");
			boolean isUrl = word.matches("https?.*");
			boolean tooShort = word.length() < 2;

			if (!isNumber && !isUrl && !tooShort && !this.irrelevantWords.contains(word)
					&& !this.emittedWords.contains(word)) {
				output.collect(new Tuple3<>(tweet, word, 1));
				this.emittedWords.add(word);
			}
		}
	}
}
