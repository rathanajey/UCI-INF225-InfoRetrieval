package searchengine.core.crawling;

import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

/**
 * Represents a default builder of a pages crawling controller
 */
public class DefaultCrawlControllerBuilder implements ICrawlControllerBuilder {
	@Override
	public CrawlController build(CrawlParameters parameters) throws Exception {
		if (parameters == null)
			throw new IllegalArgumentException("Necessary parameters for crawling are missing");

		String errorMessages = parameters.validate();

		if (errorMessages != null && errorMessages.length() > 1)
			throw new IllegalArgumentException(errorMessages);

		PageFetcher pageFetcher = new PageFetcher(parameters.getConfig());
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		CrawlController controller = new CrawlController(parameters.getConfig(), pageFetcher, robotstxtServer);

		return controller;
	}

}
