package searchengine.core;

import java.util.HashSet;

/**
 * Represents a configuration that determines the behaviour of a pages processor
 */
public class PagesProcessorConfiguration {
	public PagesProcessorConfiguration(HashSet<String> stopWords, int nGramsType, String baseSubdomain) {
		setStopWords(stopWords);
		setnGramsType(nGramsType);
		setBaseSubdomain(baseSubdomain);
	}

	private HashSet<String> stopWords;
	private int nGramsType;
	private String baseSubdomain;

	public HashSet<String> getStopWords() {
		return stopWords;
	}

	public void setStopWords(HashSet<String> stopWords) {
		this.stopWords = stopWords;
	}

	public int getNGramsType() {
		return nGramsType;
	}

	public void setnGramsType(int nGramsType) {
		if (nGramsType < 2)
			throw new IllegalArgumentException("A valid N-gram type must be at least 2");

		this.nGramsType = nGramsType;
	}

	public String getBaseSubdomain() {
		return baseSubdomain;
	}

	public void setBaseSubdomain(String baseSubdomain) {
		this.baseSubdomain = baseSubdomain;
	}
}
