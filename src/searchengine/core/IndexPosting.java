package searchengine.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a posting entry from the words index
 */
public class IndexPosting {
	public IndexPosting(int pageId, String word) {
		this(pageId, -1, word, 0, 0);
	}
	
	public IndexPosting(int pageId, int wordId, String word, int wordFrequency, double tfIdf) {
		setPageId(pageId);
		setWordId(wordId);
		setWord(word);
		
		this.wordFrequency = wordFrequency;
		
		setTfIdf(tfIdf);
		this.wordPagePositions = new ArrayList<Integer>();
	}

	private int pageId;
	private int wordId;
	private String word;
	private int wordFrequency;
	private double tfIdf;
	private List<Integer> wordPagePositions;

	public int getPageId() {
		return pageId;
	}

	public void setPageId(int pageId) {
		this.pageId = pageId;
	}

	public int getWordId() {
		return wordId;
	}

	public void setWordId(int wordId) {
		this.wordId = wordId;
	}
	
	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public int getWordFrequency() {
		return wordFrequency;
	}

	public void incrementWordFrequency() {
		wordFrequency++;
	}
	
	public double getTfIdf() {
		return tfIdf;
	}

	public void setTfIdf(double tfIdf) {
		this.tfIdf = tfIdf;
	}

	public List<Integer> getWordPagePositions() {
		return Collections.unmodifiableList(wordPagePositions);
	}

	public void addWordPagePosition(int wordPagePosition) {
		if (wordPagePosition <= 0)
			throw new IllegalArgumentException("A word position in a page cannot be lower than 1");

		wordPagePositions.add(wordPagePosition);
	}
}
