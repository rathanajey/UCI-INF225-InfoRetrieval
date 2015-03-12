package searchengine.core.repository;

/**
 * Represents a factory that creates repositories
 */
public interface IRepositoriesFactory {
	/**
	 * Gets a pages repository
	 * 
	 * @throws ClassNotFoundException
	 */
	IPagesRepository getPagesRepository() throws ClassNotFoundException;

	/**
	 * Gets a postings repository
	 * 
	 * @throws ClassNotFoundException
	 */
	IPostingsRepository getPostingsRepository() throws ClassNotFoundException;
}
