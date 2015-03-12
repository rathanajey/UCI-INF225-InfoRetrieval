package searchengine.core;

import java.sql.SQLException;

import searchengine.core.repository.IRepositoriesFactory;

/**
 * Represents a builder of the pages terms index
 */
public interface IIndexBuilder {
	/**
	 * Builds the pages term index
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException 
	 */
	void buildIndex(IRepositoriesFactory repositoriesFactory) throws ClassNotFoundException, SQLException;
}
