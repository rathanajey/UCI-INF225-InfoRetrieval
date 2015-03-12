package searchengine.core;

/**
 * Represents a text tokenizer
 */
public class Tokenizer {
	public Tokenizer(String text) {
		this.text = text;
		currentToken = null;
		textCurrentPosition = 0;
	}

	private String text;
	private String currentToken;
	private int textCurrentPosition;
	
	public String getCurrentToken() {
		return currentToken;
	}
	
	public void reset() {
		currentToken = null;
		textCurrentPosition = 0;
	}
	
	public Boolean processNextToken() {
		char[] textChars = text.toCharArray();
		int textLength = textChars.length;
		int wordStartIndex = -1;		
		
		for (int i = textCurrentPosition; i < textLength; i++) {
			// Only considers alphanumerical characters as valid for terms
			if (Character.isLetterOrDigit(textChars[i])) {
				// Stores the word's first letter index
				if (wordStartIndex < 0)
					wordStartIndex = i;

				// If it reads the last character of the page and it is
				// alphanumerical, then we have a word
				// OBS: It is necessary to increment the current index + 1
				// to allow the substring method to consider the last
				// character
				if (i == textLength - 1) {
					currentToken = text.substring(wordStartIndex, i + 1).toLowerCase();
					textCurrentPosition = i + 1;

					return true;
				}
			}

			// If it hits a non-alphanumerical character and there is a
			// word's first letter index detected, then we have a word
			else if (wordStartIndex >= 0) {
				currentToken = text.substring(wordStartIndex, i).toLowerCase();
				textCurrentPosition = i + 1;

				return true;
			}
		}
		
		currentToken = null;
		
		return false;
	}
}
