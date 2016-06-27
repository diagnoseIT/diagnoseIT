package org.diagnoseit.rules.impl;

import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.SessionVariable;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.engine.tag.Tags;
import org.diagnoseit.rules.RuleConstants;

import rocks.cta.api.core.SubTrace;
import rocks.cta.api.core.Trace;
import rocks.cta.api.core.callables.Callable;
import rocks.cta.api.core.callables.NestingCallable;
import rocks.cta.api.core.callables.TimedCallable;

/**
 * @author Alexander Wert
 *
 */
@Rule(name = "GlobalContextRule")
public class GlobalContextRule {
	private static final double PROPORTION = 0.8;

	@TagValue(type = Tags.ROOT_TAG)
	private Trace trace;

	@TagValue(type = Tags.ROOT_TAG)
	private SubTrace subTrace;
	
	
	@SessionVariable(name = RuleConstants.VAR_BASELINE, optional = false)
	private long baseline;

	@Action(resultTag = RuleConstants.TAG_GLOBAL_CONTEXT)
	public TimedCallable action() {
		long traceDuration = subTrace.getResponseTime();
		Callable root = subTrace.getRoot();
		if(!(root instanceof TimedCallable)){
			return null;
		}
		TimedCallable childWithMaxDuration = (TimedCallable) root;
		TimedCallable currentInvocationSequence;
		do {
			currentInvocationSequence = childWithMaxDuration;
			if(currentInvocationSequence instanceof NestingCallable){
				childWithMaxDuration = getChildWithMaxDuration((NestingCallable)currentInvocationSequence);
			}else{
				break;
			}
		} while (null != childWithMaxDuration && isDominatingCall(childWithMaxDuration, traceDuration));

		return currentInvocationSequence;
	}

	private boolean isDominatingCall(TimedCallable childWithMaxDuration, long traceDuration) {
		return traceDuration - childWithMaxDuration.getResponseTime() < baseline && childWithMaxDuration.getResponseTime() > traceDuration * PROPORTION;
	}

//	/**
//	 * @param childWithMaxDuration
//	 * @param nestedSequences
//	 * @return
//	 */
//	private boolean isOutlier(TimedCallable childWithMaxDuration, List<TimedCallable> nestedSequences) {
//		if (nestedSequences.size() == 1 && nestedSequences.get(0) == childWithMaxDuration) {
//			return true;
//		}
//		double sum = 0.0;
//		int numElements = nestedSequences.size() - 1;
//		double[] durations = new double[numElements];
//		int i = 0;
//		for (TimedCallable child : nestedSequences) {
//			if (child != childWithMaxDuration) { // NO-PMD not equals on purpose
//				sum += child.getResponseTime();
//				durations[i] = child.getResponseTime();
//				i++;
//			}
//		}
//		double mean = sum / numElements;
//		StandardDeviation standardDeviation = new StandardDeviation(false);
//		double sd = standardDeviation.evaluate(durations, mean);
//		return childWithMaxDuration.getResponseTime() > (mean + 3 * sd);
//	}

	/**
	 * @param currentCallable
	 * @param childWithMaxDuration
	 * @return
	 */
	private TimedCallable getChildWithMaxDuration(NestingCallable currentCallable) {
		boolean first = true;
		TimedCallable childWithMaxDuration = null;
		for (TimedCallable child : currentCallable.getCallees(TimedCallable.class)) {
			if (first) {
				childWithMaxDuration = child;
				first = false;
			} else if (child.getResponseTime() > childWithMaxDuration.getResponseTime()) {
				childWithMaxDuration = child;
			}
		}
		return childWithMaxDuration;
	}
	

}
