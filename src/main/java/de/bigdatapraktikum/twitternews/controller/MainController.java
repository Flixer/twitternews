package de.bigdatapraktikum.twitternews.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.bigdatapraktikum.twitternews.config.AppConfig;
import de.bigdatapraktikum.twitternews.parts.TwitterNewsCollector;
import de.bigdatapraktikum.twitternews.parts.TwitterNewsGraphCreator;
import de.bigdatapraktikum.twitternews.processing.TweetFilter;

@RestController
public class MainController {
	@RequestMapping(value = "/", method = RequestMethod.GET)
	protected void index(HttpServletResponse response) throws IOException {
		response.sendRedirect("/web/index.html");
	}

	@RequestMapping(value = "/collect", method = RequestMethod.GET)
	protected int collect() {
		TwitterNewsCollector collector = new TwitterNewsCollector();
		try {
			collector.execute();
		} catch (Exception e) {
			e.printStackTrace();
			return AppConfig.STATUS_INTERNAL_ERROR;
		}
		return AppConfig.STATUS_OK;
	}

	@RequestMapping(value = "/analyze", method = RequestMethod.GET)
	protected int analyze(@ModelAttribute TweetFilter filter) {
		TwitterNewsGraphCreator analyzer = new TwitterNewsGraphCreator();
		try {
			analyzer.execute(filter);
		} catch (Exception e) {
			e.printStackTrace();
			return AppConfig.STATUS_INTERNAL_ERROR;
		}
		return AppConfig.STATUS_OK;
	}

}
