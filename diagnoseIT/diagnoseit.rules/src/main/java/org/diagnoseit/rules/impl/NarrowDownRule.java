package org.diagnoseit.rules.impl;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.engine.tag.Tags;
import org.diagnoseit.rules.RuleConstants;
import org.diagnoseit.traceservices.subtrace.ExtendedSubTraceView;
import org.spec.research.open.xtrace.api.core.SubTrace;
import org.spec.research.open.xtrace.api.core.Trace;

/**
 * Rule for narrowing down a trace to its problematic subtraces
 * 
 * @author Alexander Wert, Alper Hidiroglu
 *
 */
@Rule(name = "NarrowDownRule")
public class NarrowDownRule {
	private static final long RT_THRESHOLD = 100;

	@TagValue(type = Tags.ROOT_TAG)
	private Trace trace;

	private Logger log = Logger.getGlobal();

	@Action(resultTag = RuleConstants.TAG_SUBTRACE, resultQuantity = Action.Quantity.MULTIPLE)
	public List<SubTrace> action() {
		log.info("Executing NarrowDownRule..");
		List<SubTrace> subtraces = new ArrayList<SubTrace>();
		if (!(trace.getRoot() instanceof SubTrace)) {
			log.info("Ended narrow down rule. Returned null.");
			return null;
		} else {
			if (trace.getRoot().getSubTraces().isEmpty()) {
				log.info("NarrowDownRule: Trace consists of one SubTrace");
				subtraces.add(trace.getRoot());
			} else {
				log.info("NarrowDownRule: Trace consists of more than one SubTraces");
				// Create a list with subtraces and their execution times
				List<ExtendedSubTraceView<Long>> subTraceViews = createSubTraceListWithExecutionTimes(trace);
				// first subtraces with higher exclusive times
				sortSubTracesDecendingByExecutionTime(subTraceViews);

				long traceResponseTime = trace.getResponseTime() / 1000000;
				long executionTimeSum = 0L;

				// add tags to problematic subtraces
				for (ExtendedSubTraceView<Long> stView : subTraceViews) {
					if (traceResponseTime - executionTimeSum > RT_THRESHOLD) {
						subtraces.add(stView.getOrigin());
						executionTimeSum += stView.getValue();
					} else {
						break;
					}
				}
			}
		}

		log.info("Ended narrow down rule. Returned "+ subtraces.size() + " subtraces.");
		return subtraces;
	}

	/**
	 * sort subtraces (descendent)
	 * @param subTraceViews
	 */
	private void sortSubTracesDecendingByExecutionTime(
			List<ExtendedSubTraceView<Long>> subTraceViews) {
		subTraceViews.sort(new Comparator<ExtendedSubTraceView<Long>>() {

			@Override
			public int compare(ExtendedSubTraceView<Long> o1,
					ExtendedSubTraceView<Long> o2) {
				long diff = o2.getValue() - o1.getValue();
				if (diff < 0) {
					return -1;
				} else if (diff > 0) {
					return 1;
				} else {
					return 0;
				}
			}
		});
	}

	/**
	 * Create list with subtraces and their execution times
	 * @param trace
	 * @return
	 */
	private List<ExtendedSubTraceView<Long>> createSubTraceListWithExecutionTimes(
			Trace trace) {
		List<ExtendedSubTraceView<Long>> subTraceViews = new ArrayList<ExtendedSubTraceView<Long>>();
		Iterator<SubTrace> stIterator = trace.subTraceIterator();
		while (stIterator.hasNext()) {
			SubTrace current = stIterator.next();
			long executionTime = current.getExclusiveTime();
			subTraceViews.add(new ExtendedSubTraceView<Long>(current, null,
					executionTime));
		}
		return subTraceViews;
	}

}