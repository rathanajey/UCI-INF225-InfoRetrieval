package searchengine.core;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.TreeMap;

import searchengine.core.repository.IRepositoriesFactory;

/**
 * Represents a basic processor that does a set of operations with the crawled pages
 */
public class DefaultPagesProcessor implements IPagesProcessor {
	public DefaultPagesProcessor() {
		pagesCount = 0;
		longestPageUrl = null;
		subdomainsCount = new HashMap<String, Integer>();
		mostCommonWords = new HashMap<String, Integer>();
		mostCommonNGrams = new HashMap<String, Integer>();
	}

	private final static int PAGES_CHUNK_SIZE = 64;
	private int pagesCount;
	private String longestPageUrl;
	private HashMap<String, Integer> subdomainsCount;
	private HashMap<String, Integer> mostCommonWords;
	private HashMap<String, Integer> mostCommonNGrams;

	@Override
	public void processPages(IRepositoriesFactory repositoriesFactory, PagesProcessorConfiguration config) throws SQLException, ClassNotFoundException {
		if (repositoriesFactory == null)
			throw new IllegalArgumentException("The pages processor cannot be initialized with a null repositories factory");

		if (config == null)
			throw new IllegalArgumentException("The pages processor cannot be initialized with a null configuration");

		// Makes sure the pages iteration will be from the beginning
		repositoriesFactory.getPagesRepository().reset();

		int longestPageLength = 0;
		List<Page> pages = repositoriesFactory.getPagesRepository().retrieveNextPages(PAGES_CHUNK_SIZE);

		while (pages != null && pages.size() > 0) {
			// Computes pages count
			processUniquePagesCount(pages);

			// Computes subdomains count
			processSubdomains(pages, config.getBaseSubdomain());

			// Computes most common elements
			processMostCommonElements(pages, config, repositoriesFactory);

			// Computes longest page
			longestPageLength = processLongestPage(pages, longestPageLength);

			pages = repositoriesFactory.getPagesRepository().retrieveNextPages(PAGES_CHUNK_SIZE);
		}
	}

	@Override
	public int getUniquePagesCount() {
		return pagesCount;
	}

	@Override
	public Map<String, Integer> getSubdomains() {
		// Sorts the map by its keys
		TreeMap<String, Integer> sortedMap = new TreeMap<String, Integer>(subdomainsCount);

		// Makes sure an unmodifiable result is returned
		return Collections.unmodifiableMap(sortedMap);
	}

	@Override
	public String getLongestPage() {
		return longestPageUrl;
	}

	@Override
	public Map<String, Integer> getMostCommonWords(int count) {
		// Makes sure an unmodifiable result is returned
		return Collections.unmodifiableMap(getMapFirstEntries(sortMapByValueDescending(mostCommonWords), count));
	}

	@Override
	public Map<String, Integer> getMostCommonNGrams(int count) {
		// Makes sure an unmodifiable result is returned
		return Collections.unmodifiableMap(getMapFirstEntries(sortMapByValueDescending(mostCommonNGrams), count));
	}

	private void processUniquePagesCount(List<Page> pages) {
		pagesCount += pages.size();
	}

	private void processSubdomains(List<Page> pages, String baseSubdomain) {
		for (Page page : pages) {
			String url = page.getUrl();
			String[] urlParts = url.split(baseSubdomain);

			if (urlParts != null && urlParts.length > 0) {
				String key = urlParts[0] + baseSubdomain;

				if (subdomainsCount.containsKey(key))
					subdomainsCount.put(key, subdomainsCount.get(key) + 1);
				else
					subdomainsCount.put(key, 1);
			}
		}
	}

	private void processMostCommonElements(List<Page> pages, PagesProcessorConfiguration config, IRepositoriesFactory repositoriesFactory) throws ClassNotFoundException, SQLException {
		Map<String, Map<Integer, IndexPosting>> pageIndexPostingData = new HashMap<String, Map<Integer, IndexPosting>>();
		ArrayList<Page> pagesToUpdate = new ArrayList<Page>();

		for (Page page : pages) {			
			int wordPagePosition = 0;
			Queue<String> nGramWordsQueue = new LinkedList<String>();
			Tokenizer tokenizer = new Tokenizer(page.getText());
			
			while (tokenizer.processNextToken()) {
				computeWord(config, page, tokenizer.getCurrentToken(), ++wordPagePosition, nGramWordsQueue, pageIndexPostingData);
			}				

			// If the page is not indexed
			if (!page.getIndexed()) {
				// Marks the page as indexed
				page.setIndexed(true);

				// Determines the page has to be updated in the repository
				pagesToUpdate.add(page);
			}
		}

		// If there are pages marked to be updated
		if (pagesToUpdate.size() > 0) {
			List<IndexPosting> postings = new ArrayList<IndexPosting>(pageIndexPostingData.size() * PAGES_CHUNK_SIZE);

			// Concatenates all postings from the maps buffer
			pageIndexPostingData.values().forEach(e -> postings.addAll(e.values()));

			// TODO: The two operations below should be contained within a single transaction
			repositoriesFactory.getPostingsRepository().insertPostings(postings);
			repositoriesFactory.getPagesRepository().updatePages(pagesToUpdate);
		}
	}

	private void computeWord(PagesProcessorConfiguration config, Page page, String word, int wordPagePosition, Queue<String> nGramWordsQueue, Map<String, Map<Integer, IndexPosting>> pageIndexPostingData) {		
		// Only considers non stop words and ignore overly long words
		// TODO: Include the word min/max length in the configuration/
		if ((config.getStopWords() == null || !config.getStopWords().contains(word)) && word.length() > 2 && word.length() < 64) {
			// Computes word frequency
			addToMostCommonElementMap(mostCommonWords, word);

			// Computes N-gram frequency
			// Enqueues the word
			nGramWordsQueue.add(word);

			if (nGramWordsQueue.size() == config.getNGramsType()) {
				String nGram = String.join(" ", nGramWordsQueue);

				addToMostCommonElementMap(mostCommonNGrams, nGram);

				// Dequeues the first enqueued word
				nGramWordsQueue.remove();
			}

			// If the page is not indexed, fills the word index posting data
			if (!page.getIndexed()) {
				if (!pageIndexPostingData.containsKey(word))
					pageIndexPostingData.put(word, new HashMap<Integer, IndexPosting>());

				Map<Integer, IndexPosting> postingMap = pageIndexPostingData.get(word);
				IndexPosting posting;

				posting = postingMap.containsKey(page.getId()) ? postingMap.get(page.getId()) : new IndexPosting(page.getId(), word); 
				posting.incrementWordFrequency();
				posting.addWordPagePosition(wordPagePosition);

				postingMap.put(page.getId(), posting);
				pageIndexPostingData.put(word, postingMap);
			}
		}
	}

	private void addToMostCommonElementMap(Map<String, Integer> map, String key) {
		if (map.containsKey(key))
			map.put(key, map.get(key) + 1);
		else
			map.put(key, 1);
	}

	private int processLongestPage(List<Page> pages, int longestPageLength) {
		for (Page page : pages) {
			if (page.getText().length() > longestPageLength) {
				longestPageLength = page.getText().length();
				longestPageUrl = page.getUrl();
			}
		}

		return longestPageLength;
	}

	private <K, V extends Comparable<? super V>> Map<K, V> sortMapByValueDescending(Map<K, V> map) {
		List<Entry<K, V>> sortedEntries = new ArrayList<Entry<K, V>>(map.entrySet());
		Map<K, V> sortedMap = new LinkedHashMap<K, V>();

		Collections.sort(sortedEntries, new Comparator<Entry<K, V>>() {
			@Override
			public int compare(Entry<K, V> e1, Entry<K, V> e2) {
				return e2.getValue().compareTo(e1.getValue());
			}
		});

		sortedEntries.forEach(e -> sortedMap.put(e.getKey(), e.getValue()));

		return sortedMap;
	}

	private <K, V> Map<K, V> getMapFirstEntries(Map<K, V> map, int elementsToReturn) {
		List<Entry<K, V>> entries = new ArrayList<Entry<K, V>>(map.entrySet());
		Map<K, V> reducedMap = new LinkedHashMap<K, V>();

		entries.stream().limit(elementsToReturn).forEach(e -> reducedMap.put(e.getKey(), e.getValue()));

		return reducedMap;
	}
}
