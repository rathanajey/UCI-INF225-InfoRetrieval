package searchengine.core.crawling;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import searchengine.core.Page;
import searchengine.core.repository.IPagesRepository;
import searchengine.core.repository.IRepositoriesFactory;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * Represents a crawler that visits and collects information about web pages
 */
public class Crawler extends WebCrawler {
	public Crawler() {
		pages = new ArrayList<Page>(BATCH_INSERT_LIMIT);
	}
	
	private final static int BATCH_INSERT_LIMIT = 128;
	private List<Page> pages;
	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|csv|data|java|lif|js|bmp|gif|jpe?g" + "|png|tiff?|mid|mp2|mp3|mp4" + "|wav|avi|mov|mpeg|ram|m4v|ps|ppt|pdf|pde" + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");
	private final static Pattern DOMAIN = Pattern.compile("http://.*\\.ics\\.uci\\.edu.*");
	private IPagesRepository repository;

	@Override
	public void onStart() {
		Object data = myController.getCustomData();

		if (!(data instanceof IRepositoriesFactory)) {
			throw new IllegalArgumentException("The web crawler must be supplied with a valid pages repository factory");
		}

		IRepositoriesFactory repositoriesFactory = (IRepositoriesFactory) data;
		
		try {
			repository = repositoriesFactory.getPagesRepository();
		} catch (ClassNotFoundException e) {
			printMessage("Error while creating the pages repository: " + e.getMessage());
		}
	}
	
	@Override
	public void onBeforeExit() {
		if (pages.size() > 1) {
			insertPages();
		}
		
		super.onBeforeExit();
	}

	@Override
	protected void onContentFetchError(WebURL webUrl) {
		printMessage("ERROR! Could not fetch " + webUrl.getURL());

		super.onContentFetchError(webUrl);
	}

	@Override
	protected void onParseError(WebURL webUrl) {
		printMessage("ERROR! Could not parse " + webUrl.getURL());

		super.onParseError(webUrl);
	}

	/**
	 * You should implement this function to specify whether the given url should be crawled or not (based on your crawling logic).
	 */
	@Override
	public boolean shouldVisit(WebURL url) {
		String href = url.getURL().toLowerCase();
		
		if (FILTERS.matcher(href).matches() || href.contains("?") || href.startsWith("http://fano.ics.uci.edu") || href.startsWith("http://ftp.ics.uci.edu"))
			return false;
		else
			return DOMAIN.matcher(href).matches();
	}

	/**
	 * This function is called when a page is fetched and ready to be processed by your program.
	 */
	@Override
	public void visit(edu.uci.ics.crawler4j.crawler.Page page) {
		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();

			pages.add(new Page(page.getWebURL().getURL(), htmlParseData.getText(), htmlParseData.getHtml()));

			printMessage("Crawled " + page.getWebURL().getURL());

			// If we hit the batch limit, the pages are added to the repository
			if (pages.size() == BATCH_INSERT_LIMIT) {
				insertPages();
			}
		}
	}

	// TODO: If there is a duplicate in the batch, it will fail. Make sure duplicates are updated in the database instead
	private void insertPages() {
		try {
			repository.insertPages(pages);
		} catch (SQLException e) {
			printMessage("Repository error while inserting pages: " + e.getMessage());
		}

		pages.clear();
	}

	private void printMessage(String message) {
		String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

		System.out.println("[" + currentDateTime + "] - " + message);
	}
}