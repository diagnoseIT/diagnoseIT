package org.diagnoseit.traceservices.result;

import java.util.ArrayList;
import java.util.List;

public class ProblemOccurrence {

	private Object entryPointIdentifier;
	private Object problemContextIdentifier;
	private List<Object> causeIdentifiers = new ArrayList<Object>();

	/**
	 * @return the entryPointIdentifier
	 */
	public Object getEntryPointIdentifier() {
		return entryPointIdentifier;
	}

	/**
	 * @param entryPointIdentifier
	 *            the entryPointIdentifier to set
	 */
	public void setEntryPointIdentifier(Object entryPointIdentifier) {
		this.entryPointIdentifier = entryPointIdentifier;
	}

	/**
	 * @return the problemContextIdentifier
	 */
	public Object getProblemContextIdentifier() {
		return problemContextIdentifier;
	}

	/**
	 * @param problemContextIdentifier
	 *            the problemContextIdentifier to set
	 */
	public void setProblemContextIdentifier(Object problemContextIdentifier) {
		this.problemContextIdentifier = problemContextIdentifier;
	}

	/**
	 * @return the causeIdentifiers
	 */
	public List<Object> getCauseIdentifiers() {
		return causeIdentifiers;
	}

	/**
	 * @param causeIdentifiers
	 *            the causeIdentifiers to set
	 */
	public void addCauseIdentifier(Object causeIdentifier) {
		this.causeIdentifiers.add(causeIdentifier);
	}

}
