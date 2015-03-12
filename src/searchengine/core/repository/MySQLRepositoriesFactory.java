package searchengine.core.repository;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a MySql database repositories factory
 */
// TODO: Think of a better way to structure this factory to avoid castings
public class MySQLRepositoriesFactory implements IRepositoriesFactory {
	public MySQLRepositoriesFactory() {
		repositories = new HashMap<Class<?>, Object>();
	}

	private Map<Class<?>, Object> repositories;

	@Override
	public IPagesRepository getPagesRepository() throws ClassNotFoundException {
		if (!repositories.containsKey(MySQLPagesRepository.class))
			repositories.put(MySQLPagesRepository.class, new MySQLPagesRepository());

		return (MySQLPagesRepository) repositories.get(MySQLPagesRepository.class);
	}

	@Override
	public IPostingsRepository getPostingsRepository() throws ClassNotFoundException {
		if (!repositories.containsKey(MySQLPostingsRepository.class))
			repositories.put(MySQLPostingsRepository.class, new MySQLPostingsRepository());

		return (MySQLPostingsRepository) repositories.get(MySQLPostingsRepository.class);
	}
}
