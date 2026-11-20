package alquimia.domain.service;

import alquimia.domain.exception.InconsistentMessageException;
import alquimia.domain.exception.InsufficientDataException;
import alquimia.domain.model.MessageFragment;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Rebuilds the original message from the fragments; empty words ("") are ignored. */
public class MessageReconstructionService {

	public String reconstruct(List<MessageFragment> fragments) {
		if (fragments == null || fragments.isEmpty()) {
			throw new InsufficientDataException("There are no message fragments to rebuild the message.");
		}
		int length = fragments.get(0).size();
		for (MessageFragment fragment : fragments) {
			if (fragment.size() != length) {
				throw new InconsistentMessageException("The fragments have different lengths ("
						+ length + " and " + fragment.size() + ") and cannot be aligned.");
			}
		}
		List<String> words = new ArrayList<>(length);
		for (int i = 0; i < length; i++) {
			Set<String> candidates = new LinkedHashSet<>();
			for (MessageFragment fragment : fragments) {
				String word = fragment.words().get(i);
				if (!word.isEmpty()) {
					candidates.add(word);
				}
			}
			if (candidates.isEmpty()) {
				throw new InsufficientDataException("No device received word number " + (i + 1)
						+ ", so the message cannot be rebuilt.");
			}
			if (candidates.size() > 1) {
				throw new InconsistentMessageException("The devices received different words at position "
						+ (i + 1) + ": " + candidates + ".");
			}
			words.add(candidates.iterator().next());
		}
		return String.join(" ", words);
	}
}
