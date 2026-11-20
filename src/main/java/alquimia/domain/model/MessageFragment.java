package alquimia.domain.model;

import alquimia.domain.exception.InvalidReadingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The part of Alchemy's message intercepted by one device: a list of words where a word that was not
 * received is an empty string.
 */
public final class MessageFragment {

	private final List<String> words;

	public MessageFragment(List<String> words) {
		if (words == null || words.isEmpty()) {
			throw new InvalidReadingException("The message fragment is required and cannot be an empty list.");
		}
		List<String> copy = new ArrayList<>(words.size());
		for (String word : words) {
			if (word == null) {
				throw new InvalidReadingException("The message fragment cannot contain null words; "
						+ "use an empty string for a word that was not received.");
			}
			copy.add(word.trim());
		}
		this.words = Collections.unmodifiableList(copy);
	}

	public List<String> words() {
		return words;
	}

	public int size() {
		return words.size();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof MessageFragment other && words.equals(other.words);
	}

	@Override
	public int hashCode() {
		return words.hashCode();
	}

	@Override
	public String toString() {
		return words.toString();
	}
}
