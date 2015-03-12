package searchengine.test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import searchengine.core.DefaultPagesProcessor;
import searchengine.core.Page;
import searchengine.core.PagesProcessorConfiguration;
import searchengine.core.repository.IPagesRepository;
import searchengine.core.repository.IPostingsRepository;
import searchengine.core.repository.IRepositoriesFactory;

// TODO: Test edge cases (repository is null, pages are empty, etc.)
public class DefaultPagesProcessorTest {
	private IRepositoriesFactory repositoriesFactory;
	private PagesProcessorConfiguration config;
	private final static int SAMPLE_PAGES_COUNT = 3;
	private final static int MOST_COMMON_COUNT = 2;
	private final static String LONGEST_URL = "http://graphics.ics.uci.edu/about";

	@SuppressWarnings("unchecked")
	@Before
	public final void initialize() throws SQLException, ClassNotFoundException {
		repositoriesFactory = Mockito.mock(IRepositoriesFactory.class);
		config = getTestPageProcessorConfiguration();

		Mockito.when(repositoriesFactory.getPagesRepository()).thenReturn(Mockito.mock(IPagesRepository.class));
		Mockito.when(repositoriesFactory.getPostingsRepository()).thenReturn(Mockito.mock(IPostingsRepository.class));
		Mockito.when(repositoriesFactory.getPagesRepository().retrieveNextPages(Matchers.anyInt())).thenReturn(getTestPageProcessingData(), new ArrayList<Page>());
	}

	private PagesProcessorConfiguration getTestPageProcessorConfiguration() {
		HashSet<String> stopWords = new HashSet<String>();

		try (Scanner scanner = new Scanner(DefaultPagesProcessorTest.class.getResourceAsStream("/resources/stopwords.txt"))) {
			while (scanner.hasNextLine()) {
				stopWords.add(scanner.nextLine());
			}
		}

		return new PagesProcessorConfiguration(stopWords, 2, "ics.uci.edu");
	}

	private List<Page> getTestPageProcessingData() {
		ArrayList<Page> pages = new ArrayList<Page>();

		pages.add(new Page("http://www.ics.uci.edu/about/equity/", "A sample text 1", "<html>1</html>"));
		pages.add(new Page("http://www.ics.uci.edu/about/equity/", "A sample text 2", "<html>1</html>"));
		pages.add(new Page("http://graphics.ics.uci.edu/about", "A larger sample here", "<html>1</html>"));

		return pages;
	}

	@Test
	public final void testProcess() throws SQLException, ClassNotFoundException {
		// Arrange
		DefaultPagesProcessor processor = new DefaultPagesProcessor();

		// Act
		processor.processPages(repositoriesFactory, config);

		// Assert
		Mockito.verify(repositoriesFactory.getPagesRepository(), Mockito.times(1)).reset();
		Mockito.verify(repositoriesFactory.getPagesRepository(), Mockito.times(2)).retrieveNextPages(Matchers.anyInt());
		Mockito.verify(repositoriesFactory.getPagesRepository(), Mockito.times(1)).updatePages(Matchers.any());
		Mockito.verify(repositoriesFactory.getPostingsRepository(), Mockito.times(1)).insertPostings(Matchers.any());
	}

	@Test
	public final void testGetUniquePagesCount_ProcessFinished() throws SQLException, ClassNotFoundException {
		// Arrange
		DefaultPagesProcessor processor = new DefaultPagesProcessor();
		int uniquePagesCount;

		// Act
		processor.processPages(repositoriesFactory, config);
		uniquePagesCount = processor.getUniquePagesCount();

		// Assert
		Assert.assertEquals(uniquePagesCount, SAMPLE_PAGES_COUNT);
	}

	@Test
	public final void testGetUniquePagesCount_ProcessPending() throws SQLException, ClassNotFoundException {
		// Arrange
		DefaultPagesProcessor processor = new DefaultPagesProcessor();
		int uniquePagesCount;

		// Act
		uniquePagesCount = processor.getUniquePagesCount();

		// Assert
		Assert.assertTrue(uniquePagesCount == 0);
	}

	@Test
	public final void testGetSubdomains_ProcessFinished() throws SQLException, ClassNotFoundException {
		// Arrange
		DefaultPagesProcessor processor = new DefaultPagesProcessor();
		Map<String, Integer> subdomains;

		// Act
		processor.processPages(repositoriesFactory, config);
		subdomains = processor.getSubdomains();

		// Assert
		Assert.assertTrue(subdomains.containsKey("http://graphics.ics.uci.edu"));
		Assert.assertEquals(subdomains.get("http://graphics.ics.uci.edu"), new Integer(1));
		Assert.assertTrue(subdomains.containsKey("http://www.ics.uci.edu"));
		Assert.assertEquals(subdomains.get("http://www.ics.uci.edu"), new Integer(2));
	}

	@Test
	public final void testGetSubdomains_ProcessPending() throws SQLException, ClassNotFoundException {
		// Arrange
		DefaultPagesProcessor processor = new DefaultPagesProcessor();
		Map<String, Integer> subdomains;

		// Act
		subdomains = processor.getSubdomains();

		// Assert
		Assert.assertTrue(subdomains.size() == 0);
	}

	@Test
	public final void testGetLongestPage_ProcessFinished() throws SQLException, ClassNotFoundException {
		// Arrange
		DefaultPagesProcessor processor = new DefaultPagesProcessor();
		String longestPage;

		// Act
		processor.processPages(repositoriesFactory, config);
		longestPage = processor.getLongestPage();

		// Assert
		Assert.assertEquals(longestPage, LONGEST_URL);
	}

	@Test
	public final void testGetLongestPage_ProcessPending() throws SQLException, ClassNotFoundException {
		// Arrange
		DefaultPagesProcessor processor = new DefaultPagesProcessor();
		String longestPage;

		// Act
		longestPage = processor.getLongestPage();

		// Assert
		Assert.assertNull(longestPage);
	}

	@Test
	public final void testGetMostCommonWords_ProcessFinished() throws SQLException, ClassNotFoundException {
		// Arrange
		DefaultPagesProcessor processor = new DefaultPagesProcessor();
		Map<String, Integer> mostCommonWords;

		// Act
		processor.processPages(repositoriesFactory, config);
		mostCommonWords = processor.getMostCommonWords(MOST_COMMON_COUNT);

		// Assert
		Assert.assertEquals(mostCommonWords.size(), MOST_COMMON_COUNT);
		Assert.assertTrue(mostCommonWords.containsKey("sample"));
		Assert.assertEquals(mostCommonWords.get("sample"), new Integer(SAMPLE_PAGES_COUNT));
		Assert.assertTrue(mostCommonWords.containsKey("text"));
		Assert.assertEquals(mostCommonWords.get("text"), new Integer(SAMPLE_PAGES_COUNT - 1));
	}

	@Test
	public final void testGetMostCommonWords_ProcessPending() throws SQLException, ClassNotFoundException {
		// Arrange
		DefaultPagesProcessor processor = new DefaultPagesProcessor();
		Map<String, Integer> mostCommonWords;

		// Act
		mostCommonWords = processor.getMostCommonWords(MOST_COMMON_COUNT);

		// Assert
		Assert.assertTrue(mostCommonWords.size() == 0);
	}

	@Test
	public final void testGetMostCommonNGrams_ProcessFinished() throws SQLException, ClassNotFoundException {
		DefaultPagesProcessor processor = new DefaultPagesProcessor();
		Map<String, Integer> mostCommonNGrams;

		// Act
		processor.processPages(repositoriesFactory, config);
		mostCommonNGrams = processor.getMostCommonNGrams(MOST_COMMON_COUNT);

		// Assert
		Assert.assertEquals(mostCommonNGrams.size(), MOST_COMMON_COUNT);
		Assert.assertTrue(mostCommonNGrams.containsKey("sample text"));
		Assert.assertEquals(mostCommonNGrams.get("sample text"), new Integer(SAMPLE_PAGES_COUNT - 1));
	}

	@Test
	public final void testGetMostCommonNGrams_ProcessPending() throws SQLException, ClassNotFoundException {
		DefaultPagesProcessor processor = new DefaultPagesProcessor();
		Map<String, Integer> mostCommonNGrams;

		// Act
		mostCommonNGrams = processor.getMostCommonNGrams(MOST_COMMON_COUNT);

		// Assert
		Assert.assertTrue(mostCommonNGrams.size() == 0);
	}
}
