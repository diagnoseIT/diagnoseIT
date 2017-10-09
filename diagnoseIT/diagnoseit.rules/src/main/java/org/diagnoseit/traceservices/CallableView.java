package org.diagnoseit.traceservices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.spec.research.open.xtrace.api.core.callables.Callable;
import org.spec.research.open.xtrace.api.core.callables.NestingCallable;



public class CallableView<T extends Callable> {

	private final T decoratedCallable;
	private final CallableView<? extends Callable> parent;
	private List<CallableView<? extends Callable>> children;

	public CallableView(T callable, CallableView<? extends NestingCallable> parent) {
		this.decoratedCallable = callable;
		this.parent = parent;
		if (parent != null) {
			parent.addChildView(this);
		}
	}

	private void addChildView(CallableView<? extends Callable> child) {
		if (!TraceUtils.isDescendantOf(this.getOrigin(), child.getOrigin())) {
			throw new IllegalArgumentException("Invalid descendant! Cannot add a callable view to another callable as child, if it is not a descendant of the target callable!");
		}
		if (children == null) {
			children = new ArrayList<CallableView<? extends Callable>>();
		}
		children.add(child);
	}

	public T getOrigin() {
		return decoratedCallable;
	}

	public List<CallableView<? extends Callable>> getChildren() {
		if (children == null) {
			return Collections.emptyList();
		} else {
			return Collections.unmodifiableList(children);
		}
	}

	public CallableView<? extends Callable> getParent() {
		return parent;

	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((decoratedCallable == null) ? 0 : decoratedCallable.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("unchecked")
		CallableView<? extends Callable> other = (CallableView<? extends Callable>) obj;
		if (decoratedCallable == null) {
			if (other.decoratedCallable != null)
				return false;
		} else if (!decoratedCallable.equals(other.decoratedCallable))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return decoratedCallable.toString();
	}

}
