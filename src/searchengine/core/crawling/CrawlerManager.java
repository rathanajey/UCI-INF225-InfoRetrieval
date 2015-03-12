/** 
 * Some of this code is lifted directly from the crawler4j website and we do not profess
 * to have done it ourselves.
 */
package searchengine.core.crawling;

import searchengine.core.repository.IRepositoriesFactory;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.WebCrawler;

/**
 * Represents a controller that manages the pages crawler
 */
public class CrawlerManager {
	public void Run(CrawlParameters parameters, ICrawlControllerBuilder crawlControllerBuilder, IRepositoriesFactory repositoriesFactory, Class<? extends WebCrawler> crawlerType) throws Exception {
		/*
		 * Instantiate the controller for this crawl.
		 */
		CrawlController controller = crawlControllerBuilder.build(parameters);

		/*
		 * For each crawl, you need to add some seed urls. These are the first
		 * URLs that are fetched and then the crawler starts following links
		 * which are found in these pages
		 */
		controller.addSeed(parameters.getBaseDomain());

		// Injects the repository into the crawler
		controller.setCustomData(repositoriesFactory);
		
		/*
		 * Start the crawl. This is a blocking operation, meaning that your code
		 * will reach the line after this only when crawling is finished.
		 */
		controller.start(crawlerType, parameters.getNumberOfCrawlers());
	}
}