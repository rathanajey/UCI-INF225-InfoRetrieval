package servlet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import searchengine.core.Tokenizer;

public class Ranker {
	
	private List<String> query;
	public int numOfDocs;
	public int resultsPerPage;
	
	/*
	 * Constructor takes a query string
	 */
	
	public Ranker(String query, int resultsPerPage) {
		this.query = tokenizeQuery(query);
		this.numOfDocs = 0;
		this.resultsPerPage = resultsPerPage;
		// Loads the MySQL driver
		try {
		Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Helper function for constructor, tokenizes query
	 */
	
	public static List<String> tokenizeQuery(String query){
		
		Tokenizer tokenize = new Tokenizer(query);
		List<String> data = new ArrayList<String>(10);
		while (tokenize.processNextToken()){
			
			data.add(tokenize.getCurrentToken());
		}
		return data;
	}
	
	/*
	 * The following method actually searches the database and computes the cosine
	 * similarity for each document to the query. This will be stored in our internal
	 * HashMap.
	 */
	
	public List<String> search(int page) throws SQLException{
		
		List<String> results = new ArrayList<String>(resultsPerPage);
		try (Connection connection = getConnection()) {
			// ResultSet.TYPE_SCROLL_SENSITIVE tells the driver to consider altered records since the last page was read
			// ResultSet.CONCUR_READ_ONLY tells the driver to create a read-only result set
			try (Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
				
				StringBuilder build = new StringBuilder();
				String sql;

				build.append("select P.URL, SUM(WP.TFIDF) as totalTfidf from wordspages WP inner join pages P on WP.PageId = P.Id where wordId in (select Id from words where Word in (");
				for (String s: query){
					build.append("\"");					
					build.append(s);
					build.append("\"");
					if (query.indexOf(s) == query.size() - 1)
						break;
					build.append(",");
				}
				build.append(")) group by WP.PageId order by totalTfidf desc");
				sql = build.toString();

				try (ResultSet resultSet = statement.executeQuery(sql)) {
					
					resultSet.absolute((page-1)*resultsPerPage);
					int i = 0;
					while (resultSet.next() && i < resultsPerPage){
						results.add(resultSet.getString("URL"));
						i++;
					}
					
					resultSet.last();
					numOfDocs = resultSet.getRow();
					resultSet.close();
				}
			}
		}
		return results;
	}	
	
	private Connection getConnection() throws SQLException {
		// TODO: make connection parameters configurable
		// useServerPrepStmts=false tells MySQL to handle server-side prepared statements locally
		// rewriteBatchedStatements=true tells MySQL to pack as many queries as possible into a single network packet
		return DriverManager.getConnection("jdbc:mysql://localhost:3306/ucicrawling?user=root&password=password&useServerPrepStmts=false&rewriteBatchedStatements=true&useUnicode=true&characterEncoding=UTF-8");
	}
	 
	
	

}
