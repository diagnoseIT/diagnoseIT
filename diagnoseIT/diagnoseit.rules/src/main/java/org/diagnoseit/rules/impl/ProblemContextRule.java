package org.diagnoseit.rules.impl;

import java.util.HashMap;
import java.util.Map;

import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.SessionVariable;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.rules.RuleConstants;
import org.diagnoseit.spike.result.ProblemInstance;
import org.diagnoseit.spike.traceservices.TraceUtils;
import org.diagnoseit.spike.traceservices.aggregation.AbstractAggregatedTimedCallable;
import org.diagnoseit.spike.traceservices.decorator.callable.ExtendedCallableView;
import org.spec.research.open.xtrace.api.core.callables.Callable;
import org.spec.research.open.xtrace.api.core.callables.NestingCallable;
import org.spec.research.open.xtrace.api.core.callables.TimedCallable;

/**
 * Rule for detecting the local problem context of a time wasting operation
 * 
 * @author Alexander Wert
 *
 */
@Rule(name = "ProblemContextRule")
public class ProblemContextRule {
	private static final Double RT_RALATION_THRESHOLD = 0.8;

	@TagValue(type = RuleConstants.TAG_TIME_WASTING_OPERATIONS)
	private AbstractAggregatedTimedCallable<? extends TimedCallable> timeWastingOperation;

	@SessionVariable(name = RuleConstants.VAR_BASELINE, optional = false)
	private double baseline;

	@Action(resultTag = RuleConstants.TAG_PROBLEM_CONTEXT)
	public TimedCallable action() {
		System.out.println("ProblemContextRule wird gefeuert!");
		TimedCallable problemContext = findProblemLocalContext(timeWastingOperation);
		System.out.println("Der Problem Context ist: " + problemContext);

		return problemContext;

	}

	private ExtendedCallableView<Long, ? extends TimedCallable> buildUpPath(
			Callable globalContext,
			Map<Integer, ExtendedCallableView<Long, TimedCallable>> knownRoots,
			TimedCallable currentCallable, long value) {
		if (currentCallable == globalContext.getParent()) {
			return null;
		}
		int currentHash = currentCallable.hashCode();
		ExtendedCallableView<Long, ? extends TimedCallable> parent = buildUpPath(
				globalContext, knownRoots, currentCallable.getParent(), value);
		if (!knownRoots.containsKey(currentHash)) {
			if (parent == null) {
				knownRoots.put(currentHash,
						new ExtendedCallableView<Long, TimedCallable>(
								currentCallable, null, 0L));
			} else {
				knownRoots
						.put(currentHash,
								new ExtendedCallableView<Long, TimedCallable>(
										currentCallable,
										(ExtendedCallableView<Long, NestingCallable>) parent,
										0L));
			}
		}
		ExtendedCallableView<Long, TimedCallable> ecv = knownRoots
				.get(currentHash);
		long newValue = ecv.getValue() + value;
		ecv.setValue(newValue);
		return ecv;
	}

	private TimedCallable findProblemLocalContext(
			AbstractAggregatedTimedCallable<? extends TimedCallable> timeWastingOperation) {
		if (timeWastingOperation.getCallables().size() == 1) {
			return timeWastingOperation.getCallables().get(0);
		}

		// rebuilds trace in a bottom-up way while aggregating the execution
		// time of the target
		// operation and annotating corresponding ancestor nodes
		ExtendedCallableView<Long, ? extends TimedCallable> viewRoot = createExtendedTrace(timeWastingOperation);

		ExtendedCallableView<Long, ? extends TimedCallable> localProblemContext = findProblemHotSpot(viewRoot);

		return localProblemContext.getOrigin();
	}

	private ExtendedCallableView<Long, ? extends TimedCallable> findProblemHotSpot(
			ExtendedCallableView<Long, ? extends TimedCallable> current) {
		long rootValue = current.getValue();
		ExtendedCallableView<Long, ? extends TimedCallable> next = current;

		do {
			current = next;

			next = null;
			boolean first = true;
			for (Object child : current.getChildren()) {
				ExtendedCallableView<Long, ? extends TimedCallable> extChild = (ExtendedCallableView<Long, ? extends TimedCallable>) child;
				if (first) {
					next = extChild;
					first = false;
				} else if ((extChild.getValue()) > (next.getValue())) {
					next = extChild;
				}
			}
		} while (next != null
				&& (next.getValue()) > Math.round(((double) rootValue)
						* RT_RALATION_THRESHOLD));
		return current;
	}

	private ExtendedCallableView<Long, ? extends TimedCallable> createExtendedTrace(
			AbstractAggregatedTimedCallable<? extends TimedCallable> timeWastingOperation) {
		Callable globalContext = timeWastingOperation.getGlobalContext();
		ExtendedCallableView<Long, ? extends TimedCallable> viewRoot = null;
		Map<Integer, ExtendedCallableView<Long, TimedCallable>> knownRoots = new HashMap<Integer, ExtendedCallableView<Long, TimedCallable>>();

		for (TimedCallable currentCallable : timeWastingOperation
				.getCallables()) {
			ExtendedCallableView<Long, ? extends TimedCallable> ecv = buildUpPath(
					globalContext, knownRoots, currentCallable,
					(currentCallable.getExclusiveTime() / 1000000));
			if (viewRoot == null) {
				viewRoot = ecv;
				while (viewRoot.getParent() != null) {
					viewRoot = (ExtendedCallableView<Long, TimedCallable>) viewRoot
							.getParent();
				}
			}
		}
		return viewRoot;
	}

}