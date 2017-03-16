//package org.diagnoseit.rules.mobile.impl;
//
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.Iterator;
//import java.util.List;
//
//import org.diagnoseit.engine.rule.annotation.Action;
//import org.diagnoseit.engine.rule.annotation.Rule;
//import org.diagnoseit.engine.rule.annotation.TagValue;
//import org.diagnoseit.engine.tag.Tags;
//import org.diagnoseit.rules.RuleConstants;
//import org.diagnoseit.traceservices.subtrace.ExtendedSubTraceView;
//import org.spec.research.open.xtrace.api.core.SubTrace;
//import org.spec.research.open.xtrace.api.core.Trace;
//
///**
// * Rule for identifying problematic subtraces
// * 
// * @author Alexander Wert, Alper Hidiroglu
// *
// */
//@Rule(name = "NarrowDownRule")
//public class NarrowDownRule {
//	private static final long RT_THRESHOLD = 1000;
//
//	@TagValue(type = RuleConstants.TAG_JAVA_AGENT_SUBTRACE)
//	private SubTrace subTrace;
//
//	@Action(resultTag = RuleConstants.TAG_SUBTRACE, resultQuantity = Action.Quantity.MULTIPLE)
//	public List<SubTrace> action() {
//		System.out.println("Executing NarrowDownRule..");
//		List<SubTrace> subtraces = new ArrayList<SubTrace>();
//		if (subTrace.getSubTraces().isEmpty()) {
//			System.out.println("NarrowDownRule: Only one SubTrace");
//			subtraces.add(subTrace);
//		} else {
//			System.out.println("NarrowDownRule: More than one SubTrace");
//
//			long subTraceResponseTime = subTrace.getResponseTime() / 1000000;
//
//			if (subTraceResponseTime < RT_THRESHOLD) {
//				return null;
//			}
//
//			// Create a list with subtraces and their execution times
//			List<ExtendedSubTraceView<Long>> subTraceViews = createSubTraceListWithExecutionTimes(subTrace);
//			// first subtraces with higher exclusive times
//			sortSubTracesDecendingByExecutionTime(subTraceViews);
//
//			long executionTimeSum = 0L;
//
//			// add tags to problematic subtraces
//			for (ExtendedSubTraceView<Long> stView : subTraceViews) {
//				if (subTraceResponseTime - executionTimeSum > RT_THRESHOLD) {
//					subtraces.add(stView.getOrigin());
//					executionTimeSum += stView.getValue();
//				} else {
//					break;
//				}
//			}
//		}
//
//		return subtraces;
//	}
//
//	/**
//	 * sort subtraces (descendent)
//	 * 
//	 * @param subTraceViews
//	 */
//	private void sortSubTracesDecendingByExecutionTime(
//			List<ExtendedSubTraceView<Long>> subTraceViews) {
//		subTraceViews.sort(new Comparator<ExtendedSubTraceView<Long>>() {
//
//			@Override
//			public int compare(ExtendedSubTraceView<Long> o1,
//					ExtendedSubTraceView<Long> o2) {
//				long diff = o2.getValue() - o1.getValue();
//				if (diff < 0) {
//					return -1;
//				} else if (diff > 0) {
//					return 1;
//				} else {
//					return 0;
//				}
//			}
//		});
//	}
//
//	/**
//	 * Create list with subtraces and their execution times
//	 * 
//	 * @param trace
//	 * @return
//	 */
//	private List<ExtendedSubTraceView<Long>> createSubTraceListWithExecutionTimes(
//			SubTrace subTrace) {
//		List<ExtendedSubTraceView<Long>> subTraceViews = new ArrayList<ExtendedSubTraceView<Long>>();
//		List<SubTrace> rootList = new ArrayList<SubTrace>(1);
//		rootList.add(subTrace);
//		Iterator<SubTrace> stIterator = rootList.iterator();
//		while (stIterator.hasNext()) {
//			SubTrace current = stIterator.next();
//			long executionTime = current.getExclusiveTime();
//			subTraceViews.add(new ExtendedSubTraceView<Long>(current, null,
//					executionTime));
//		}
//		return subTraceViews;
//	}
//
//}