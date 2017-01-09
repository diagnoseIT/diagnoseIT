package org.diagnoseit.traceservices.subtrace;

import java.util.Iterator;

import org.spec.research.open.xtrace.api.core.SubTrace;

public abstract class AbstractSubTraceVisitor {
	private Iterator<SubTrace> iterator;

	/**
	 * @param iterator
	 */
	public AbstractSubTraceVisitor(Iterator<SubTrace> iterator) {
		this.iterator = iterator;
	}
	
	public void visitAll(){
		while(iterator.hasNext()){
			visit(iterator.next());
		}
	}
	
	protected abstract void visit(SubTrace subTrace);
	
	
}
