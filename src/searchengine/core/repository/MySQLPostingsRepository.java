package searchengine.core.repository;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import searchengine.core.IndexPosting;

/**
 * Represents a MySql database that contains data about the index postings
 */
// TODO: this class is not unit-testable. Some parameters need to be injected
public class MySQLPostingsRepository implements IPostingsRepository {
	public MySQLPostingsRepository() throws ClassNotFoundException {
		// Loads the MySQL driver
		Class.forName("com.mysql.jdbc.Driver");

		reset();
		
		wordsIdsCache = new HashMap<String, Integer>();
	}

	// Since the database records are read in chunks (or pages), determines the
	// current posting that will be read
	private int currentPostingsPaginationIndex;
	
	// Caches the recently inserted words ids to diminish the number of reads in the database
	// TODO: this cache is not thread-safe
	private Map<String, Integer> wordsIdsCache;

	@Override
	public void reset() {
		currentPostingsPaginationIndex = 0;
	}

	@Override
	public List<IndexPosting> retrieveNextPostings(int postingsChunkSize) throws SQLException {
		List<IndexPosting> postings = new ArrayList<IndexPosting>();

		try (Connection connection = getConnection()) {
			// ResultSet.TYPE_SCROLL_SENSITIVE tells the driver to consider altered records since the last page was read
			// ResultSet.CONCUR_READ_ONLY tells the driver to create a read-only result set
			try (Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
				// Tells the drivers the expected result set size in advance to enhance performance
				statement.setFetchSize(postingsChunkSize);
				statement.setMaxRows(postingsChunkSize);

				String sql = "SELECT W.Id, W.Word, WP.PageId, WP.Frequency, WP.TFIDF FROM words W INNER JOIN wordspages WP ON W.Id = WP.WordId LIMIT " + currentPostingsPaginationIndex + ", " + postingsChunkSize;

				try (ResultSet resultSet = statement.executeQuery(sql)) {
					while (resultSet.next()) {
						int wordId = resultSet.getInt(1);
						String word = resultSet.getString(2);
						int pageId = resultSet.getInt(3);
						int frequency = resultSet.getInt(4);
						double tfidf = resultSet.getDouble(5);

						IndexPosting posting = new IndexPosting(pageId, wordId, word, frequency, tfidf);

						// TODO: Just make one single select that brings all positions from all related pages and words to avoid multiple database reads
						try (PreparedStatement positionsStatement = connection.prepareStatement("SELECT Position from wordspagespositions WHERE WordId = ? AND PageId = ?")) {
							positionsStatement.setInt(1, posting.getWordId());
							positionsStatement.setInt(2, posting.getPageId());

							try (ResultSet positionsResultSet = positionsStatement.executeQuery()) {
								while (positionsResultSet.next()) {
									int position = positionsResultSet.getInt(1);

									posting.addWordPagePosition(position);
								}
							}
						}

						postings.add(posting);
					}
				}
			}
		}

		currentPostingsPaginationIndex += postingsChunkSize;

		return postings;
	}

	@Override
	public int insertPostings(List<IndexPosting> postings) throws SQLException {
		int updateCounts = 0;

		if (postings != null) {
			try (Connection connection = getConnection()) {
				try {					
					connection.setAutoCommit(false);

					try (PreparedStatement insertWordsPagesPositionsStatement = connection.prepareStatement("INSERT INTO wordspagespositions (WordId, PageId, Position) VALUES (?, ?, ?)")) {
						try (PreparedStatement insertWordsPagesStatement = connection.prepareStatement("INSERT INTO wordspages (WordId, PageId, Frequency, TFIDF) VALUES (?, ?, ?, ?)")) {
							for (IndexPosting posting : postings) {
								// If the word id is present in the map, it has already been inserted into the database
								if (!wordsIdsCache.containsKey(posting.getWord())) {
									try (PreparedStatement selectWordStatement = connection.prepareStatement("SELECT Id, Word FROM words WHERE word = ?")) {
										selectWordStatement.setString(1, posting.getWord());

										// TODO: Just make one single select that brings all positions from all related pages and words to avoid multiple database reads
										// Checks if the word has already been inserted in the database in a previous chunk
										try (ResultSet selectWordResultSet = selectWordStatement.executeQuery()) {
											if (selectWordResultSet.next()) {
												int currentWordId = selectWordResultSet.getInt(1);
												posting.setWordId(currentWordId);
												wordsIdsCache.put(posting.getWord(), currentWordId);
											} else {
												// Inserts the word in the database if it was not found
												try (PreparedStatement insertWordStatement = connection.prepareStatement("INSERT INTO words (Word) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
													insertWordStatement.setString(1, posting.getWord());

													insertWordStatement.executeUpdate();

													updateCounts++;

													// Gets the auto-generated id from the database and sets to the word
													try (ResultSet insertWordResultSet = insertWordStatement.getGeneratedKeys()) {
														insertWordResultSet.next();

														int currentWordId = insertWordResultSet.getInt(1);
														posting.setWordId(currentWordId);
														wordsIdsCache.put(posting.getWord(), currentWordId);
													}
												}
											}
										}
									}
								} else {
									posting.setWordId(wordsIdsCache.get(posting.getWord()));
								}

								insertWordsPagesStatement.setInt(1, posting.getWordId());
								insertWordsPagesStatement.setInt(2, posting.getPageId());
								insertWordsPagesStatement.setInt(3, posting.getWordFrequency());
								insertWordsPagesStatement.setDouble(4, posting.getTfIdf());

								insertWordsPagesStatement.addBatch();

								if (posting.getWordPagePositions() != null) {
									for (int position : posting.getWordPagePositions()) {
										insertWordsPagesPositionsStatement.setInt(1, posting.getWordId());
										insertWordsPagesPositionsStatement.setInt(2, posting.getPageId());
										insertWordsPagesPositionsStatement.setInt(3, position);

										insertWordsPagesPositionsStatement.addBatch();
									}
								}
							}

							updateCounts += insertWordsPagesStatement.executeBatch().length;
							updateCounts += insertWordsPagesPositionsStatement.executeBatch().length;

							connection.commit();
						}
					}
				} catch (Exception e) {
					connection.rollback();
					connection.setAutoCommit(true);
					throw e;
				}
			}
		}

		return updateCounts;
	}

	@Override
	public int[] updatePostings(List<IndexPosting> postings) throws SQLException {
		int[] updateCounts = null;

		if (postings != null) {
			try (Connection connection = getConnection()) {
				try (PreparedStatement statement = connection.prepareStatement("UPDATE wordspages SET TFIDF = ? WHERE WordId = ? AND PageId = ?")) {
					for (IndexPosting posting : postings) {
						statement.setDouble(1, posting.getTfIdf());
						statement.setInt(2, posting.getWordId());
						statement.setInt(3, posting.getPageId());

						statement.addBatch();
					}

					updateCounts = statement.executeBatch();
				}
			}
		}

		return updateCounts;
	}

	@Override
	public int[] deletePostings(List<IndexPosting> postings) throws SQLException {
		int[] deleteCountArray = null;

		if (postings != null) {
			try (Connection connection = getConnection()) {
				try (PreparedStatement statement = connection.prepareStatement("DELETE FROM words WHERE Id = ?")) {
					for (IndexPosting posting : postings) {
						statement.setInt(1, posting.getWordId());

						statement.addBatch();
					}

					deleteCountArray = statement.executeBatch();
					
					// If the delete was successful, removes words ids from the cache
					for (IndexPosting posting : postings) {
						wordsIdsCache.remove(posting.getWordId());
					}
				}
			}
		}

		return deleteCountArray;
	}
	
	@Override
	public void calculatePostingsRankingScore() throws SQLException {
		try (Connection connection = getConnection()) {
			try (CallableStatement statement = connection.prepareCall("call calculatePostingsRankingScore")) {
				statement.execute();
			}
		}
	}
	
	private Connection getConnection() throws SQLException {
		// TODO: make connection parameters configurable
		// useServerPrepStmts=false tells MySQL to handle server-side prepared statements locally
		// rewriteBatchedStatements=true tells MySQL to pack as many queries as possible into a single network packet
		return DriverManager.getConnection("jdbc:mysql://localhost:3306/ucicrawling?user=root&password=password&useServerPrepStmts=false&rewriteBatchedStatements=true&useUnicode=true&characterEncoding=UTF-8");
	}	
}
