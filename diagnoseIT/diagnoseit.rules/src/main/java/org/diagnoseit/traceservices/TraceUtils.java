package org.diagnoseit.traceservices;

import java.util.ArrayList;
import java.util.List;

import org.spec.research.open.xtrace.api.core.SubTrace;
import org.spec.research.open.xtrace.api.core.callables.Callable;
import org.spec.research.open.xtrace.api.core.callables.NestingCallable;

public class TraceUtils {

	public static boolean isDescendantOf(Callable ancestor, Callable subject) {
		Callable current = subject;
		while (current != null) {
			if (current.equals(ancestor)) {
				return true;
			}
			current = current.getParent();
		}
		return false;
	}

	public static boolean isDescendantOf(SubTrace ancestor, SubTrace subject) {
		SubTrace current = subject;
		while (current != null) {
			if (current.equals(ancestor)) {
				return true;
			}
			current = current.getParent();
		}
		return false;
	}

	public static List<Callable> asList(Callable rootCallable) {
		List<Callable> processingList = new ArrayList<Callable>();

		processingList.add(rootCallable);
	
		int idx = 0;
		while (idx < processingList.size()) {
			Callable current = processingList.get(idx);
			if(current instanceof NestingCallable){
				NestingCallable nestingCurrent = (NestingCallable)current;
				List<Callable> children = nestingCurrent.getCallees();
				if (children != null && !children.isEmpty()) {
					processingList.addAll(nestingCurrent.getCallees());
				}
			}
		

			idx++;
		}
		return processingList;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Callable> List<T> asList(Callable rootCallable, Class<T> typeFilter) {
		List<Callable> preList = asList(rootCallable);
		List<T> result = new ArrayList<T>();
		for(Callable clbl : preList){
			if(typeFilter.isAssignableFrom(clbl.getClass())){
				result.add((T)clbl);
			}
		}

		return result;
	}

	public static List<SubTrace> asList(SubTrace rootSubTrace) {
		List<SubTrace> processingList = new ArrayList<SubTrace>();

		processingList.add(rootSubTrace);
		int idx = 0;
		while (idx < processingList.size()) {
			SubTrace current = processingList.get(idx);
			List<SubTrace> children = current.getSubTraces();
			if (children != null && !children.isEmpty()) {
				processingList.addAll(current.getSubTraces());
			}

			idx++;
		}
		return processingList;
	}
}
