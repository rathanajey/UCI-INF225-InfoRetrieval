package searchengine.test;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import searchengine.core.crawling.CrawlParameters;
import searchengine.core.crawling.Crawler;
import searchengine.core.crawling.CrawlerManager;
import searchengine.core.crawling.ICrawlControllerBuilder;
import searchengine.core.repository.IPagesRepository;
import searchengine.core.repository.IRepositoriesFactory;
import edu.uci.ics.crawler4j.crawler.CrawlController;

public class CrawlerManagerTest {
	private CrawlParameters parameters;
	private ICrawlControllerBuilder crawlControllerBuilder;
	private IRepositoriesFactory repositoriesFactory;
	private CrawlController controller;
	
	@Before
	public final void initialize() throws Exception {
		parameters = Mockito.mock(CrawlParameters.class);
		crawlControllerBuilder = Mockito.mock(ICrawlControllerBuilder.class);
		repositoriesFactory = Mockito.mock(IRepositoriesFactory.class);
		controller = Mockito.mock(CrawlController.class);
		
		Mockito.when(parameters.validate()).thenReturn(null);
		Mockito.when(crawlControllerBuilder.build(parameters)).thenReturn(controller);
		Mockito.when(repositoriesFactory.getPagesRepository()).thenReturn(Mockito.mock(IPagesRepository.class));
	}

	@Test
	public final void testRun() throws Exception {
		CrawlerManager manager = new CrawlerManager();
		
		// Act
		manager.Run(parameters, crawlControllerBuilder, repositoriesFactory, Crawler.class);
		
		// Assert
		Mockito.verify(crawlControllerBuilder).build(parameters);		
		Mockito.verify(controller).addSeed(parameters.getBaseDomain());
		Mockito.verify(controller).setCustomData(repositoriesFactory);
		Mockito.verify(controller).start(Crawler.class, parameters.getNumberOfCrawlers());
	}

}
