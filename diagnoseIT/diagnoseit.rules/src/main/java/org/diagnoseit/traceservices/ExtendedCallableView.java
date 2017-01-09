package org.diagnoseit.traceservices;

import org.spec.research.open.xtrace.api.core.callables.Callable;
import org.spec.research.open.xtrace.api.core.callables.NestingCallable;



public class ExtendedCallableView<T, E extends Callable> extends CallableView<E> {
	private T value;

	public ExtendedCallableView(E callable, CallableView<? extends NestingCallable> parent, T initialValue) {
		super(callable, parent);
		value=initialValue;
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
