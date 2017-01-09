package org.diagnoseit.traceservices.subtrace;

import org.spec.research.open.xtrace.api.core.SubTrace;

public class ExtendedSubTraceView<T> extends SubTraceView {

	private T value;

	public ExtendedSubTraceView(SubTrace subTrace, SubTraceView parent, T initialValue) {
		super(subTrace, parent);
		value = initialValue;
	}

	/**
	 * @return the value
	 */
	public T getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(T value) {
		this.value = value;
	}

}
