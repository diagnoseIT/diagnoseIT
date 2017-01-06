package org.diagnoseit.rules.impl;

import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.SessionVariable;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.rules.RuleConstants;
import org.spec.research.open.xtrace.api.core.SubTrace;
import org.spec.research.open.xtrace.api.core.callables.Callable;
import org.spec.research.open.xtrace.api.core.callables.NestingCallable;
import org.spec.research.open.xtrace.api.core.callables.TimedCallable;

/**
 * Rule for detecting the global context within a subtrace
 * 
 * @author Alexander Wert, Alper Hidiroglu
 *
 */
@Rule(name = "GlobalContextRule")
public class GlobalContextRule {
	private static final double PROPORTION = 0.8;

	// @TagValue(type = Tags.ROOT_TAG)
	// private Trace trace;

	@TagValue(type = RuleConstants.TAG_SUBTRACE)
	private SubTrace subTrace;

	@SessionVariable(name = RuleConstants.VAR_BASELINE, optional = false)
	private double baseline;

	@Action(resultTag = RuleConstants.TAG_GLOBAL_CONTEXT)
	public TimedCallable action() {
		System.out.println("GlobalContextRule wird gefeuert!");

		long traceDuration = subTrace.getResponseTime() / 1000000;
		Callable root = subTrace.getRoot();
		System.out.println("Der Root Knoten im SubTrace ist: "
				+ subTrace.getRoot());
		// Root has to be a TimedCallable
		if (!(root instanceof TimedCallable)) {
			return null;
		}
		// Find global context
		TimedCallable childWithMaxDuration = (TimedCallable) root;
		TimedCallable currentInvocationSequence;
		do {
			currentInvocationSequence = childWithMaxDuration;
			// NestingCallables can hold further Callables
			if (currentInvocationSequence instanceof NestingCallable) {
				// System.out.println("nesting");
				childWithMaxDuration = getChildWithMaxDuration((NestingCallable) currentInvocationSequence);
			} else {
				break;
			}
			// If child node is not a dominant call return the parent node
		} while (null != childWithMaxDuration
				&& isDominatingCall(
						childWithMaxDuration.getResponseTime() / 1000000,
						traceDuration));

		System.out.println("The Global Context is: "
				+ currentInvocationSequence);
		return currentInvocationSequence;
	}

	// Investigate if current node is a dominant call
	private boolean isDominatingCall(long durationOfChild, long traceDuration) {
		return traceDuration - durationOfChild < baseline
				&& durationOfChild > traceDuration * PROPORTION;
	}

	// /**
	// * @param childWithMaxDuration
	// * @param nestedSequences
	// * @return
	// */
	// private boolean isOutlier(TimedCallable childWithMaxDuration,
	// List<TimedCallable> nestedSequences) {
	// if (nestedSequences.size() == 1 && nestedSequences.get(0) ==
	// childWithMaxDuration) {
	// return true;
	// }
	// double sum = 0.0;
	// int numElements = nestedSequences.size() - 1;
	// double[] durations = new double[numElements];
	// int i = 0;
	// for (TimedCallable child : nestedSequences) {
	// if (child != childWithMaxDuration) { // NO-PMD not equals on purpose
	// sum += child.getResponseTime();
	// durations[i] = child.getResponseTime();
	// i++;
	// }
	// }
	// double mean = sum / numElements;
	// StandardDeviation standardDeviation = new StandardDeviation(false);
	// double sd = standardDeviation.evaluate(durations, mean);
	// return childWithMaxDuration.getResponseTime() > (mean + 3 * sd);
	// }

	/**
	 * Find the child with the highest duration (start from current Callable)
	 * 
	 * @param currentCallable
	 * @param childWithMaxDuration
	 * @return
	 */
	private TimedCallable getChildWithMaxDuration(
			NestingCallable currentCallable) {
		boolean first = true;
		TimedCallable childWithMaxDuration = null;
		// Finds the child with the highest duration
		for (TimedCallable child : currentCallable
				.getCallees(TimedCallable.class)) {
			if (first) {
				childWithMaxDuration = child;
				first = false;
			} else if (child.getResponseTime() / 1000000 > childWithMaxDuration
					.getResponseTime() / 1000000) {
				childWithMaxDuration = child;
			}
		}
		return childWithMaxDuration;
	}

}
