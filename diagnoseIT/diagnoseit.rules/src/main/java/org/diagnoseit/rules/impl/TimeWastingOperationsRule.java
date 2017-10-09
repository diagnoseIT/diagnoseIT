package org.diagnoseit.rules.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.SessionVariable;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.rules.RuleConstants;
import org.diagnoseit.traceservices.TraceUtils;
import org.diagnoseit.traceservices.aggregated.AbstractAggregatedTimedCallable;
import org.spec.research.open.xtrace.api.core.callables.TimedCallable;

/**
 * Rule for detecting time wasting operations within a tree of callables (start
 * at global context as root element)
 * 
 * @author Alexander Wert
 *
 */
@Rule(name = "TimeWastingOperationsRule")
public class TimeWastingOperationsRule {
	private static final Double RT_RALATION_THRESHOLD = 0.8;

	@SessionVariable(name = RuleConstants.VAR_BASELINE, optional = false)
	private double baseline;

	@TagValue(type = RuleConstants.TAG_GLOBAL_CONTEXT)
	private TimedCallable globalContext;

	// There can be more than one Time wasting operations
	@Action(resultTag = RuleConstants.TAG_TIME_WASTING_OPERATIONS, resultQuantity = Action.Quantity.MULTIPLE)
	public List<AbstractAggregatedTimedCallable<? extends TimedCallable>> action() {
		System.out.println("Executing TimeWastingOperationsRule..");

		// Create a list and add global context with its children. Then add
		// their children etc.
		List<TimedCallable> processingList = TraceUtils.asList(globalContext,
				TimedCallable.class);

		List<AbstractAggregatedTimedCallable<? extends TimedCallable>> aggregatedCallables;

		// Callables from processingList are aggregated
		aggregatedCallables = aggregateCallables(processingList);

		// Sort the aggregatedCallables with their exclusive times (descendent)
		sortAggCallablesDescending(aggregatedCallables);

		List<AbstractAggregatedTimedCallable<? extends TimedCallable>> timeWastingOperations = new ArrayList<>();
		double sumExecTime = 0;
		// Iterate over all aggregatedCallables and decide, which one is time
		// wasting
		for (AbstractAggregatedTimedCallable<? extends TimedCallable> aggCallable : aggregatedCallables) {
			aggCallable.setGlobalContext(globalContext);

			// The whole exclusive time of all Callables in the current
			// aggregated callable
			long aggExecTime = aggCallable.getExclusiveTimeStats().getSum();
			// problematic aggregated Callables
			if ((globalContext.getResponseTime() / 1000000) - sumExecTime > baseline
					|| sumExecTime < RT_RALATION_THRESHOLD
							* (globalContext.getResponseTime() / 1000000)) {
				sumExecTime += aggExecTime;

				timeWastingOperations.add(aggCallable);
			} else {
				break;
			}
		}

		return timeWastingOperations;
	}

	// sort Callables
	private void sortAggCallablesDescending(
			List<AbstractAggregatedTimedCallable<? extends TimedCallable>> aggregatedCallables) {
		aggregatedCallables
				.sort(new Comparator<AbstractAggregatedTimedCallable<? extends TimedCallable>>() {

					@Override
					public int compare(
							AbstractAggregatedTimedCallable<? extends TimedCallable> o1,
							AbstractAggregatedTimedCallable<? extends TimedCallable> o2) {
						// sort descending!
						long diff = o2.getExclusiveTimeStats().getSum()
								- o1.getExclusiveTimeStats().getSum();
						if (diff < 0L) {
							return -1;
						} else if (diff > 0L) {
							return 1;
						}
						return 0;
					}
				});
	}

	private List<AbstractAggregatedTimedCallable<? extends TimedCallable>> aggregateCallables(
			List<TimedCallable> processingList) {

		// List for aggregated Callables
		List<AbstractAggregatedTimedCallable<? extends TimedCallable>> aggregatedCallables = new ArrayList<AbstractAggregatedTimedCallable<? extends TimedCallable>>();

		// aggregate Callables
		for (TimedCallable callable : processingList) {
			boolean aggregated = false;

			// In the first iteration create first a aggregated callable
			for (AbstractAggregatedTimedCallable<? extends TimedCallable> aggCallable : aggregatedCallables) {
				// when possible, add Callable to aggregation
				aggregated = aggCallable.aggregate(callable);

				if (aggregated) {
					break;
				}
			}

			if (!aggregated) {
				// with the current callable create a aggregated callable
				AbstractAggregatedTimedCallable<? extends TimedCallable> aggClbl = AbstractAggregatedTimedCallable
						.createAggregatedCallable(callable, true);
				// add callable to aggregation
				aggClbl.aggregate(callable);
				// methods are aggregated, that have the same signature
				// respectively when database calls then aggregating same SQL
				// statements
				aggregatedCallables.add(aggClbl);
			}
		}
		return aggregatedCallables;
	}

}
