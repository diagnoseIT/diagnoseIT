package org.diagnoseit.rules;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.spec.research.open.xtrace.api.core.callables.Callable;
import org.spec.research.open.xtrace.api.core.callables.NestingCallable;
import org.spec.research.open.xtrace.api.core.callables.TimedCallable;

/**
 * Iterator for the invocation tree of an {@link InvocationSequenceData}.
 *
 * This iterator visits the {@link InvocationSequenceData} elements in the same
 * order as the corresponding methods have been originally called in the calling
 * tree.
 *
 * @author Alexander Wert, Alper Hidiroglu
 *
 */
public class TimedCallableIterator implements Iterator<TimedCallable> {

	/**
	 * Next element in the iteration.
	 */
	private TimedCallable next;

	/**
	 * The {@link InvocationSequenceData} element determining the iteration end.
	 */
	private final TimedCallable iterationEnd;

	/**
	 * Current depth in the calling tree.
	 */
	private int depth = -1;

	/**
	 * Next calculated depth.
	 */
	private int nextDepth = 0;

	/**
	 * Constructor.
	 *
	 * @param startFrom
	 *            the {@link InvocationSequenceData} element to start the
	 *            iteration from.
	 */
	public TimedCallableIterator(TimedCallable startFrom) {
		this(startFrom, false);
	}

	/**
	 * Constructor.
	 *
	 * @param startFrom
	 *            the {@link InvocationSequenceData} element to start the
	 *            iteration from.
	 * @param onlySubTree
	 *            if true, only the sub-tree of the given
	 *            {@link InvocationSequenceData} element is iterated. If false,
	 *            parent items (located after the given
	 *            {@link InvocationSequenceData} element) are iterated as well.
	 */
	public TimedCallableIterator(TimedCallable startFrom, boolean onlySubTree) {
		if (null == startFrom) {
			throw new IllegalArgumentException(
					"Cannot iterate on a null invocation sequence.");
		}
		iterationEnd = onlySubTree ? startFrom.getParent() : null;

		this.next = startFrom;
		while (null != startFrom.getParent()) {
			nextDepth++;
			startFrom = startFrom.getParent();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext() {
		return null != next;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TimedCallable next() {
		TimedCallable result = next;
		depth = nextDepth;
		next = findNext(next, null);
		return result;
	}

	/**
	 * Returns the depth in the calling tree of the element returned by the last
	 * call to the {@link #next()} method.
	 *
	 * @return Returns the depth in the calling tree of the element returned by
	 *         the last call to the {@link #next()} method.
	 */
	public int currentDepth() {
		return depth;
	}

	/**
	 * Determines the next element for iteration.
	 *
	 * @param current
	 *            current element in iteration.
	 * @param child
	 *            the previous child element in the children list of the current
	 *            {@link InvocationSequenceData} element.
	 * @return Returns the next element for iteration.
	 */
	private TimedCallable findNext(TimedCallable current, TimedCallable child) {
		if (iterationEnd == current) {
			return null;
		}

		List<Callable> nestedSequencesCallable = ((NestingCallable) current)
				.getCallees();
		List<TimedCallable> nestedSequences = new ArrayList<TimedCallable>();
		for (Callable nesting : nestedSequencesCallable) {
			nestedSequences.add((TimedCallable) nesting);
		}

		if (nestedSequences.isEmpty()) {
			nextDepth--;
			return findNext(current.getParent(), current);
		} else {
			int childIndex = -1;
			if (null != child) {
				for (TimedCallable childCandidate : nestedSequences) {
					childIndex++;
					if (childCandidate == child) { // NO-PMD equals on purpose
						break;
					}
				}
				if (childIndex >= nestedSequences.size()) {
					throw new IllegalStateException(
							"Parent list does not contain this child invocation sequence.");
				}
			}

			int nextIndex = childIndex + 1;
			if (nextIndex < nestedSequences.size()) {
				nextDepth++;
				return nestedSequences.get(nextIndex);
			} else {
				nextDepth--;
				return findNext(current.getParent(), current);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException(
				"Modifications of invocation sequences through this iterator are not allowed.");
	}

}
