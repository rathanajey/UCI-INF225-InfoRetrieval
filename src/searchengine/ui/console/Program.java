package searchengine.ui.console;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import searchengine.core.DefaultIndexBuilder;
import searchengine.core.DefaultPagesProcessor;
import searchengine.core.IIndexBuilder;
import searchengine.core.IPagesProcessor;
import searchengine.core.PagesProcessorConfiguration;
import searchengine.core.crawling.CrawlParameters;
import searchengine.core.crawling.Crawler;
import searchengine.core.crawling.CrawlerManager;
import searchengine.core.crawling.DefaultCrawlControllerBuilder;
import searchengine.core.crawling.ICrawlControllerBuilder;
import searchengine.core.repository.IRepositoriesFactory;
import searchengine.core.repository.MySQLRepositoriesFactory;
import searchengine.test.DefaultPagesProcessorTest;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;

public class Program {
	private final static int POLITENESS_DELAY = 500;
	private final static int MAX_DEPTH_OF_CRAWLING = 40;
	private final static int MAX_PAGES_TO_FETCH = 60000;
	private final static int NUMBER_OF_CRAWLERS = 5;
	private final static int N_GRAM_TYPE = 2;
	private final static int MOST_FREQUENT_WORDS_COUNT = 500;
	private final static int MOST_FREQUENT_N_GRAMS_COUNT = 20;
	private final static String CRAWLING_AGENT_NAME = "UCI WebCrawler 93082117/30489978/12409858";
	private final static String BASE_DOMAIN = "ics.uci.edu";
	private final static String BASE_DOMAIN_URL = "http://www." + BASE_DOMAIN;

	public static void main(String[] args) {
		try {
			int option;
			final String NOT_PROCESSED_ERROR_MESSAGE = "\nPages must be processed first\n";
			IRepositoriesFactory repositoriesFactory = new MySQLRepositoriesFactory();
			IIndexBuilder indexBuilder = new DefaultIndexBuilder();
			IPagesProcessor processor = null;

			try (Scanner stdin = new Scanner(System.in)) {
				do {
					printOptions();

					option = stdin.nextInt();

					switch (option) {
					case 1: {
						long startTime = System.currentTimeMillis();

						crawl(repositoriesFactory);

						long elapsedTime = System.currentTimeMillis() - startTime;

						String formattedElapsedTime = formatElapsedTime(elapsedTime);

						System.out.println("\nPages crawled in " + formattedElapsedTime + "\n");
						break;
					}
					case 2: {
						long startTime = System.currentTimeMillis();

						processor = processPages(repositoriesFactory);

						long elapsedTime = System.currentTimeMillis() - startTime;

						String formattedElapsedTime = formatElapsedTime(elapsedTime);

						System.out.println("\nPages processed in " + formattedElapsedTime + "\n");
						break;
					}
					case 3: {
						if (processor == null) {
							System.out.println(NOT_PROCESSED_ERROR_MESSAGE);
						} else {
							System.out.println("\n" + processor.getUniquePagesCount() + "\n");
						}
						break;
					}
					case 4: {
						if (processor == null) {
							System.out.println(NOT_PROCESSED_ERROR_MESSAGE);
						} else {
							System.out.println("\n" + processor.getLongestPage() + "\n");
						}
						break;
					}
					case 5: {
						if (processor == null) {
							System.out.println(NOT_PROCESSED_ERROR_MESSAGE);
						} else {
							Map<String, Integer> mostFrequentWords = processor.getMostCommonWords(MOST_FREQUENT_WORDS_COUNT);

							displayMapResult(mostFrequentWords, "MostFrequentWords.txt");

							System.out.println();
						}
						break;
					}
					case 6: {
						if (processor == null) {
							System.out.println(NOT_PROCESSED_ERROR_MESSAGE);
						} else {
							Map<String, Integer> mostFrequent2Grams = processor.getMostCommonNGrams(MOST_FREQUENT_N_GRAMS_COUNT);

							displayMapResult(mostFrequent2Grams, "MostFrequent2Grams.txt");

							System.out.println();
						}
						break;
					}
					case 7: {
						if (processor == null) {
							System.out.println(NOT_PROCESSED_ERROR_MESSAGE);
						} else {
							Map<String, Integer> subdomains = processor.getSubdomains();

							displayMapResult(subdomains, "Subdomains.txt");

							System.out.println();
						}
						break;
					}
					case 8: {
						long startTime = System.currentTimeMillis();

						indexBuilder.buildIndex(repositoriesFactory);

						long elapsedTime = System.currentTimeMillis() - startTime;

						String formattedElapsedTime = formatElapsedTime(elapsedTime);

						System.out.println("\nIndex built in " + formattedElapsedTime + "\n");
						break;
					}
					}
				} while (option != 0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void printOptions() {
		System.out.println("(1) - Crawl UCI's domain");
		System.out.println("(2) - Process crawled pages data");
		System.out.println("(3) - Display unique pages count");
		System.out.println("(4) - Display longest page URL");
		System.out.println("(5) - Display top " + MOST_FREQUENT_WORDS_COUNT + " most frequent words");
		System.out.println("(6) - Display top " + MOST_FREQUENT_N_GRAMS_COUNT + " most frequent " + N_GRAM_TYPE + "-grams");
		System.out.println("(7) - Display subdomains");
		System.out.println("(8) - Build index");		

		System.out.println("(0) - Exit");
	}

	private static String formatElapsedTime(long elapsedTime) {
		return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(elapsedTime), TimeUnit.MILLISECONDS.toMinutes(elapsedTime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsedTime)),
				TimeUnit.MILLISECONDS.toSeconds(elapsedTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedTime)));
	}

	private static void displayMapResult(Map<String, Integer> subdomains, String filePath) throws FileNotFoundException {
		Set<Entry<String, Integer>> entries = subdomains.entrySet();

		System.out.println();

		for (Entry<String, Integer> entry : entries) {
			System.out.println(entry.getKey() + " - " + entry.getValue());
		}

		try (PrintWriter printer = new PrintWriter(filePath)) {
			for (Entry<String, Integer> entry : entries) {
				printer.println(entry.getKey() + " - " + entry.getValue());
			}
		}
	}

	private static void crawl(IRepositoriesFactory repositoriesFactory) throws Exception {
		CrawlerManager manager = new CrawlerManager();
		CrawlConfig config = new CrawlConfig();
		ICrawlControllerBuilder crawlControllerBuilder = new DefaultCrawlControllerBuilder();

		config.setCrawlStorageFolder(".\\data\\crawl\\root");
		config.setPolitenessDelay(POLITENESS_DELAY);
		config.setMaxDepthOfCrawling(MAX_DEPTH_OF_CRAWLING);
		config.setMaxPagesToFetch(MAX_PAGES_TO_FETCH);
		config.setUserAgentString(CRAWLING_AGENT_NAME);
		config.setResumableCrawling(true);

		manager.Run(new CrawlParameters(config, NUMBER_OF_CRAWLERS, BASE_DOMAIN_URL), crawlControllerBuilder, repositoriesFactory, Crawler.class);
	}

	private static IPagesProcessor processPages(IRepositoriesFactory repositoriesFactory) throws SQLException, ClassNotFoundException {
		IPagesProcessor processor = new DefaultPagesProcessor();
		HashSet<String> stopWords = new HashSet<String>();

		try (Scanner scanner = new Scanner(DefaultPagesProcessorTest.class.getResourceAsStream("/resources/stopwords.txt"))) {
			while (scanner.hasNextLine()) {
				stopWords.add(scanner.nextLine());
			}
		}

		processor.processPages(repositoriesFactory, new PagesProcessorConfiguration(stopWords, N_GRAM_TYPE, "ics.uci.edu"));

		return processor;
	}	
}
