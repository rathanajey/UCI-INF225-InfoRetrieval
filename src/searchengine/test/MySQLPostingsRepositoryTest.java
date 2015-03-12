package searchengine.test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import searchengine.core.IndexPosting;
import searchengine.core.Page;
import searchengine.core.repository.IPagesRepository;
import searchengine.core.repository.IPostingsRepository;
import searchengine.core.repository.MySQLPagesRepository;
import searchengine.core.repository.MySQLPostingsRepository;

//TODO: rewrite this class so that tests are not strongly coupled to the DB as they are for now
//TODO: this needs more tests (reset scenario, many batch inserts and reads, etc.)
public class MySQLPostingsRepositoryTest {
	private IPagesRepository pagesRepository;
	private IPostingsRepository postingsRepository;
	private List<Page> pages;
	private List<IndexPosting> postings;
	private final static int UPDATES_COUNT = 10;

	@Before
	public final void initialize() throws SQLException, ClassNotFoundException {
		pagesRepository = new MySQLPagesRepository();
		postingsRepository = new MySQLPostingsRepository();

		pages = getTestPages();

		pagesRepository.insertPages(pages);

		postings = getTestPostings(pages);
	}

	@After
	public final void finalize() throws SQLException {
		pagesRepository.deletePages(pages);
	}

	private List<Page> getTestPages() {
		List<Page> pages = new ArrayList<Page>();

		pages.add(new Page("www.testurl1.com", "word1 word2 word1", "<html>1</html>"));
		pages.add(new Page("www.testurl2.com", "word1 word1", "<html>2</html>"));

		return pages;
	}

	private List<IndexPosting> getTestPostings(List<Page> pages) {
		List<IndexPosting> postings = new ArrayList<IndexPosting>();

		IndexPosting posting = new IndexPosting(pages.get(0).getId(), -1, "word1", 2, 0);

		posting.addWordPagePosition(1);
		posting.addWordPagePosition(3);

		postings.add(posting);

		posting = new IndexPosting(pages.get(0).getId(), -1, "word2", 1, 0);

		posting.addWordPagePosition(2);

		postings.add(posting);

		posting = new IndexPosting(pages.get(1).getId(), -1, "word1", 2, 0);

		posting.addWordPagePosition(1);
		posting.addWordPagePosition(2);

		postings.add(posting);

		return postings;
	}

	@Test
	public void testRetrievePostings() throws SQLException {
		// Arrange
		List<IndexPosting> retrievedPostings;

		// Act
		postingsRepository.insertPostings(postings);
		retrievedPostings = postingsRepository.retrieveNextPostings(3);
		postingsRepository.deletePostings(postings);

		// Assert
		Assert.assertTrue(retrievedPostings != null && retrievedPostings.size() == 3);

		for (IndexPosting posting : retrievedPostings) {
			Assert.assertTrue(posting.getWordPagePositions() != null && posting.getWordPagePositions().size() > 0);
		}
	}

	@Test
	public void testInsertPostings() throws SQLException {
		// Arrange
		int result;

		// Act
		result = postingsRepository.insertPostings(postings);
		postingsRepository.deletePostings(postings);

		// Assert
		Assert.assertTrue(result == UPDATES_COUNT);
	}

	@Test
	public void testUpdatePostings() throws SQLException {
		// Arrange
		int[] result;

		// Act
		postingsRepository.insertPostings(postings);

		postings.forEach(p -> p.setTfIdf(1d));

		result = postingsRepository.updatePostings(postings);
		postingsRepository.deletePostings(postings);

		// Assert
		Assert.assertTrue(result != null && result.length == postings.size());
	}

	@Test
	public void testDeletePostings() throws SQLException {
		// Arrange
		int[] result;

		// Act
		postingsRepository.insertPostings(postings);
		result = postingsRepository.deletePostings(postings);

		// Assert
		Assert.assertTrue(result != null && result.length == postings.size());
	}
	
	@Test
	public void testCalculateRankingsScore() throws SQLException {
		// TODO: requires implementation
		Assert.fail();
	}
}
