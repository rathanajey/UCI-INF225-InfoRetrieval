package searchengine.core;

/**
 * Represents a lightweight version of a Page that only contains relevant
 * processing data
 */
public class Page {
	public Page(String url, String text, String html) {
		this(-1, url, text, html, false);
	}
	
	public Page(int id, String url, String text, String html, Boolean indexed) {
		setId(id);
		setUrl(url);
		setText(text);
		setHtml(html);
		setIndexed(indexed);
	}

	private int id;
	private String url;
	private String text;
	private String html;
	private Boolean indexed;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String Url) {
		this.url = Url;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public Boolean getIndexed() {
		return indexed;
	}

	public void setIndexed(Boolean indexed) {
		this.indexed = indexed;
	}
}
