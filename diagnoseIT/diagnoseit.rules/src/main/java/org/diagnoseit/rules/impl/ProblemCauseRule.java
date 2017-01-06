package org.diagnoseit.rules.impl;

import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.SessionVariable;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.rules.RuleConstants;
import org.diagnoseit.spike.result.ProblemInstance;
import org.diagnoseit.spike.traceservices.TraceUtils;
import org.diagnoseit.spike.traceservices.aggregation.AbstractAggregatedTimedCallable;
import org.spec.research.open.xtrace.api.core.callables.TimedCallable;

/**
 * Problem Cause Rule holt die Causes des Problem Contextes, schaut welche davon
 * time wasting sind und aggregiert diese zu einem problem cause
 *
 * @author Alexander Wert
 **/
@Rule(name = "ProblemCauseRule")
public class ProblemCauseRule {
	// private static final Double RT_RALATION_THRESHOLD = 0.8;

	@TagValue(type = RuleConstants.TAG_TIME_WASTING_OPERATIONS)
	private AbstractAggregatedTimedCallable<? extends TimedCallable> timeWastingOperation;

	@TagValue(type = RuleConstants.TAG_PROBLEM_CONTEXT)
	private TimedCallable problemContext;

	@SessionVariable(name = RuleConstants.VAR_BASELINE, optional = false)
	private double baseline;

	@Action(resultTag = RuleConstants.TAG_PROBLEM_CAUSE)
	public AbstractAggregatedTimedCallable<? extends TimedCallable> action() {
		System.out.println("ProblemCauseRule wird gefeuert!");

		// exclude callables that are not descendants of the context callable
		AbstractAggregatedTimedCallable<? extends TimedCallable> newAggregatedCallable = AbstractAggregatedTimedCallable
				.createAggregatedCallable(timeWastingOperation.getCallables()
						.get(0), true);
		for (TimedCallable clbl : timeWastingOperation.getCallables()) {
			if (TraceUtils.isDescendantOf(problemContext, clbl)) {
				newAggregatedCallable.aggregate(clbl);
			}
		}
		// problemCauseTag.setCause(newAggregatedCallable);

//		ProblemInstance problemInstance = new ProblemInstance(problemContext,
//				newAggregatedCallable, (long) baseline);
//		// List<NewProblemTag> contextTags = new ArrayList<NewProblemTag>();
//		// System.out.println(problemInstance.getNodeType());
//		newAggregatedCallable.setProblemInstance(problemInstance);
		// contextTags.add(npTag);
		System.out.println("Der Typ des Problem Cause ist: "
				+ newAggregatedCallable.getType());
//		System.out
//				.println("Die Ursache des Problem Cause (bzw. des Problem Instances) ist: "
//						+ newAggregatedCallable.getProblemInstance().getCause());
		return newAggregatedCallable;
	}
}