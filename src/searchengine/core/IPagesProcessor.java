package searchengine.core;

import java.sql.SQLException;
import java.util.Map;

import searchengine.core.repository.IRepositoriesFactory;

/**
 * Represents a processor that does a set of operations with the crawled pages
 */
public interface IPagesProcessor {
	/**
	 * Processes pages retrieved from a repository
	 * 
	 * @param config
	 *            The configuration that should be considered during the processing
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	/*
	 * The reason for one single process method is efficiency. By doing this, we are able to iterate the pages only once and compute all necessary information
	 */
	void processPages(IRepositoriesFactory repositoriesFactory, PagesProcessorConfiguration config) throws SQLException, ClassNotFoundException;

	/**
	 * Returns the number of unique crawled pages
	 */
	int getUniquePagesCount();

	/**
	 * Gets the number of subdomains found per page
	 */
	Map<String, Integer> getSubdomains();

	/**
	 * Gets the crawled page with the longest plain text
	 */
	String getLongestPage();

	/**
	 * Gets the most common words found in the crawled pages plain text
	 * 
	 * @param count
	 *            The top N words that should be returned
	 */
	Map<String, Integer> getMostCommonWords(int count);

	/**
	 * Gets the most common N-grams found in the crawled pages plain text
	 *
	 * @param count
	 *            The top N N-grams that should be returned
	 */
	Map<String, Integer> getMostCommonNGrams(int count);
}
