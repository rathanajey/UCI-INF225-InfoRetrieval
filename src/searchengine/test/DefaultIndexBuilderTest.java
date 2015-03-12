package searchengine.test;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import searchengine.core.DefaultIndexBuilder;
import searchengine.core.repository.IPostingsRepository;
import searchengine.core.repository.IRepositoriesFactory;

//TODO: Test edge cases (repository is null, postings are empty, etc.)
public class DefaultIndexBuilderTest {
	private IRepositoriesFactory repositoriesFactory;
	
	@Before
	public final void initialize() throws Exception {
		repositoriesFactory = Mockito.mock(IRepositoriesFactory.class);
		
		Mockito.when(repositoriesFactory.getPostingsRepository()).thenReturn(Mockito.mock(IPostingsRepository.class));		
	}
	
	@Test
	public void testBuidIndex_ExistingPostings() throws ClassNotFoundException, SQLException {
		// Arrange
		DefaultIndexBuilder indexBuilder = new DefaultIndexBuilder(); 
		
		// Act
		indexBuilder.buildIndex(repositoriesFactory);
		
		// Assert		
		Mockito.verify(repositoriesFactory.getPostingsRepository(), Mockito.times(1)).calculatePostingsRankingScore();
	}
}
