package org.diagnoseit.traceservices.subtrace;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.diagnoseit.traceservices.TraceUtils;
import org.spec.research.open.xtrace.api.core.Location;
import org.spec.research.open.xtrace.api.core.SubTrace;
import org.spec.research.open.xtrace.api.core.Trace;
import org.spec.research.open.xtrace.api.core.TreeIterator;
import org.spec.research.open.xtrace.api.core.callables.Callable;
import org.spec.research.open.xtrace.api.utils.CallableIteratorOnTrace;



public class SubTraceView implements SubTrace {

	private final SubTrace decoratedSubTrace;
	private final SubTraceView parent;
	private List<SubTrace> children;
	private int childCount = 0;

	public SubTraceView(SubTrace subTrace, SubTraceView parent) {
		this.decoratedSubTrace = subTrace;
		this.parent = parent;
		if (parent != null) {
			parent.addChildView(this);
		}
	}

	private void addChildView(SubTraceView child) {
		if (!TraceUtils.isDescendantOf(this.getOrigin(), child.getOrigin())) {
			throw new IllegalArgumentException("Invalid descendant! Cannot add a callable view to another callable as child, if it is not a descendant of the target callable!");
		}
		if (children == null) {
			children = new ArrayList<SubTrace>();
		}
		updateChildCount(child.getChildCount() + 1);
		children.add(child);
	}

	public SubTrace getOrigin() {
		return decoratedSubTrace;
	}

	@Override
	public TreeIterator<Callable> iterator() {
		return new CallableIteratorOnTrace(this);
	}

	@Override
	public Trace getContainingTrace() {
		return decoratedSubTrace.getContainingTrace();
	}


	@Override
	public Location getLocation() {
		return decoratedSubTrace.getLocation();
	}

	@Override
	public SubTrace getParent() {
		return parent;
	}

	@Override
	public Callable getRoot() {
		return decoratedSubTrace.getRoot();
	}

	@Override
	public List<SubTrace> getSubTraces() {
		return children;
	}

	@Override
	public int size() {
		return decoratedSubTrace.size();
	}

	/**
	 * Updates the child count of this node by increasing the current child count by the passed
	 * childCountIncrease.
	 * 
	 * @param childCountIncrease
	 *            the childCount to increment the current child count by
	 */
	private void updateChildCount(int childCountIncrease) {
		this.childCount += childCountIncrease;
		if (parent != null) {
			parent.updateChildCount(childCountIncrease);
		}
	}

	public int getChildCount() {
		return childCount;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((decoratedSubTrace == null) ? 0 : decoratedSubTrace.hashCode());
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
		SubTraceView other = (SubTraceView) obj;
		if (decoratedSubTrace == null) {
			if (other.decoratedSubTrace != null)
				return false;
		} else if (!decoratedSubTrace.equals(other.decoratedSubTrace))
			return false;
		return true;
	}

	@Override
	public long getExclusiveTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getResponseTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Optional<Object> getIdentifier() {
		return Optional.empty();
	}

	@Override
	public void setIdentifier(Object arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public long getSubTraceId() {
		return decoratedSubTrace.getSubTraceId();
	}

}
