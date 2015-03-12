package searchengine.core.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import searchengine.core.Page;

/**
 * Represents a MySql database that contains data about the crawled pages
 */
// TODO: this class is not unit-testable. Some parameters need to be injected
public class MySQLPagesRepository implements IPagesRepository {
	public MySQLPagesRepository() throws ClassNotFoundException {
		// Loads the MySQL driver
		Class.forName("com.mysql.jdbc.Driver");

		reset();
	}

	// Since the database records are read in chunks (or pages), determines the
	// current page that will be read
	private int currentPagesPaginationIndex;

	@Override
	public void reset() {
		currentPagesPaginationIndex = 0;
	}

	@Override
	public List<Page> retrieveNextPages(int pagesChunkSize) throws SQLException {
		List<Page> pages = new ArrayList<Page>();

		try (Connection connection = getConnection()) {
			// ResultSet.TYPE_SCROLL_SENSITIVE tells the driver to consider altered records since the last page was read
			// ResultSet.CONCUR_READ_ONLY tells the driver to create a read-only result set
			try (Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
				// Tells the drivers the expected result set size in advance to enhance performance
				statement.setFetchSize(pagesChunkSize);
				statement.setMaxRows(pagesChunkSize);

				String sql = "SELECT Id, URL, Text, Indexed FROM pages LIMIT " + currentPagesPaginationIndex + ", " + pagesChunkSize;

				try (ResultSet resultSet = statement.executeQuery(sql)) {
					while (resultSet.next()) {
						// TODO: for this version, the page's HTML content is not being read from the DB
						Page page = new Page(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3), "", resultSet.getBoolean(4));

						pages.add(page);
					}
				}
			}
		}

		currentPagesPaginationIndex += pagesChunkSize;

		return pages;
	}

	@Override
	public int[] insertPages(List<Page> pages) throws SQLException {
		int[] updateCounts = null;

		if (pages != null) {
			try (Connection connection = getConnection()) {
				try (PreparedStatement statement = connection.prepareStatement("INSERT INTO pages (URL, Text, HTML, Indexed) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
					for (Page page : pages) {
						statement.setString(1, page.getUrl());
						statement.setString(2, page.getText());
						statement.setString(3, page.getHtml());
						statement.setBoolean(4, page.getIndexed());

						statement.addBatch();
					}

					updateCounts = statement.executeBatch();
					
					// Gets the auto-generated ids from the database and sets to the pages
					try (ResultSet resultSet = statement.getGeneratedKeys()) {
						for (Page page : pages) {
							resultSet.next();
							page.setId(resultSet.getInt(1));
						}
					}
				}
			}
		}

		return updateCounts;
	}

	@Override
	public int[] updatePages(List<Page> pages) throws SQLException {
		int[] updateCounts = null;

		if (pages != null) {
			try (Connection connection = getConnection()) {
				try (PreparedStatement statement = connection.prepareStatement("UPDATE pages SET Indexed = ? WHERE Id = ?")) {
					for (Page page : pages) {
						statement.setBoolean(1, page.getIndexed());
						statement.setInt(2, page.getId());

						statement.addBatch();
					}

					updateCounts = statement.executeBatch();
				}
			}
		}

		return updateCounts;
	}

	@Override
	public int[] deletePages(List<Page> pages) throws SQLException {
		int[] deleteCountArray = null;

		if (pages != null) {
			try (Connection connection = getConnection()) {
				try (PreparedStatement statement = connection.prepareStatement("DELETE FROM pages WHERE Id = ?")) {
					for (Page page : pages) {
						statement.setInt(1, page.getId());

						statement.addBatch();
					}

					deleteCountArray = statement.executeBatch();
				}
			}
		}

		return deleteCountArray;
	}

	private Connection getConnection() throws SQLException {
		// TODO: make connection parameters configurable
		// useServerPrepStmts=false tells MySQL to handle server-side prepared statements locally
		// rewriteBatchedStatements=true tells MySQL to pack as many queries as possible into a single network packet
		return DriverManager.getConnection("jdbc:mysql://localhost:3306/ucicrawling?user=root&password=password&useServerPrepStmts=false&rewriteBatchedStatements=true&useUnicode=true&characterEncoding=UTF-8");
	}
}
