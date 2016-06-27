package org.diagnoseit.engine.rule;

import java.util.Collection;
import java.util.Collections;

import org.diagnoseit.engine.tag.Tag;

/**
 * Value object defining the input for a single rule execution.
 *
 * @author Claudio Waldvogel
 * @see Tag
 */
public class RuleInput {

	/**
	 * The root {@link Tag} which is need by a {@link RuleDefinition} to execute.
	 *
	 * @see RuleDefinition
	 * @see Tag
	 */
	private final Tag root;

	/**
	 * A collection of {@link Tag}s which were extracted from the {@link #root} {@link Tag}. The
	 * exact content of the unraveled collection depends on which {@link Tag}s the actual rule
	 * implementation needs to execute.. The {@link #root} Tag itself is present in the unraveled
	 * collection as well.
	 *
	 * @see FireCondition
	 * @see TagInjection
	 */
	private final Collection<Tag> unraveled;

	/**
	 * Default Constructor.
	 *
	 * @param root
	 *            The root {@link Tag}.
	 */
	public RuleInput(Tag root) {
		this(root, Collections.singleton(root));
	}

	/**
	 * Constructor with unraveled collection.
	 *
	 * @param root
	 *            The root {@link Tag}.
	 * @param unraveled
	 *            The unraveled collection of Tags. ({@link #unraveled}).
	 * @see Tag
	 */
	public RuleInput(Tag root, Collection<Tag> unraveled) {
		this.root = root;
		this.unraveled = unraveled;
	}

	/**
	 * Gets {@link #root}.
	 *
	 * @return {@link #root}
	 */
	public Tag getRoot() {
		return root;
	}

	/**
	 * Gets {@link #unraveled}.
	 *
	 * @return {@link #unraveled}
	 */
	public Collection<Tag> getUnraveled() {
		return unraveled;
	}
}
