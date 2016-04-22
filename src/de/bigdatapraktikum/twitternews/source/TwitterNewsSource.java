package de.bigdatapraktikum.twitternews.source;

import java.time.LocalDateTime;

import org.apache.flink.streaming.api.functions.source.SourceFunction;

public class TwitterNewsSource implements SourceFunction<Tweet> {
	private static final long serialVersionUID = 1L;
	private boolean isRunning = false;

	@Override
	public void run(SourceContext<Tweet> context) throws Exception {
		isRunning = true;
		while (isRunning) {
			// TODO Receive data from twitter (for multiple companies) and
			// return it
			Tweet tweet = new Tweet(LocalDateTime.of(2016, 04, 2, 5, 8), "FAZ.net",
					"\"Auf dem Land sollte die Integration besser gelingen\", sagt Landwirtschaftsminister Schmidt im F.A.Z.-Interview:",
					2);
			context.collect(tweet);
			isRunning = false;
		}
	}

	@Override
	public void cancel() {
		isRunning = false;
	}
}
