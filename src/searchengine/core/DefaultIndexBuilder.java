package searchengine.core;

import java.sql.SQLException;

import searchengine.core.repository.IRepositoriesFactory;

/**
 * Represents a basic builder of the pages terms index
 */
public class DefaultIndexBuilder implements IIndexBuilder {
	@Override
	public void buildIndex(IRepositoriesFactory repositoriesFactory) throws ClassNotFoundException, SQLException {
		if (repositoriesFactory == null)
			throw new IllegalArgumentException("The pages processor cannot be initialized with a null repositories factory");

		repositoriesFactory.getPostingsRepository().calculatePostingsRankingScore();
	}
}
