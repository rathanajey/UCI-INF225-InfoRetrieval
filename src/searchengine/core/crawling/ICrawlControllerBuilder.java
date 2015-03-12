package searchengine.core.crawling;

import edu.uci.ics.crawler4j.crawler.CrawlController;

/**
 * Represents a builder of pages crawling controllers
 */
public interface ICrawlControllerBuilder {
	/**
	 * Builds a pages crawling controller given a set of parameters
	 * @throws Exception 
	 */
	CrawlController build(CrawlParameters parameters) throws Exception;
}
