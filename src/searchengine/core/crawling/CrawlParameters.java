package searchengine.core.crawling;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;

/**
 * Represents a set of parameters that configure the web pages crawling
 * operation
 */
public class CrawlParameters {
	public CrawlParameters(CrawlConfig config, int numberOfCrawlers, String baseDomain) {
		setConfig(config);
		setNumberOfCrawlers(numberOfCrawlers);
		setBaseDomain(baseDomain);
	}

	private CrawlConfig config;
	private int numberOfCrawlers;
	private String baseDomain;

	public String validate() {
		StringBuilder errorMessages = new StringBuilder();

		if (getConfig() == null)
			errorMessages.append("Crawling configuration is missing\n");

		if (getNumberOfCrawlers() <= 0)
			errorMessages.append("Number of crawlers cannot be lower than 1\n");

		if (getBaseDomain() == null || getBaseDomain() == "")
			errorMessages.append("Base domain is missing\n");

		return errorMessages.toString();
	}

	public CrawlConfig getConfig() {
		return config;
	}

	public void setConfig(CrawlConfig config) {
		this.config = config;
	}

	public int getNumberOfCrawlers() {
		return numberOfCrawlers;
	}

	public void setNumberOfCrawlers(int numberOfCrawlers) {
		this.numberOfCrawlers = numberOfCrawlers;
	}

	public String getBaseDomain() {
		return baseDomain;
	}

	public void setBaseDomain(String baseDomain) {
		this.baseDomain = baseDomain;
	}	
}
