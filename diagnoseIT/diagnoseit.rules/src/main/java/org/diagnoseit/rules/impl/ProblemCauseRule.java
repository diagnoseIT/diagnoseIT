package org.diagnoseit.rules.impl;


import java.util.logging.Logger;

import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.SessionVariable;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.rules.RuleConstants;
import org.diagnoseit.traceservices.TraceUtils;
import org.diagnoseit.traceservices.aggregated.AbstractAggregatedTimedCallable;
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

	private Logger log = Logger.getGlobal();

	@TagValue(type = RuleConstants.TAG_TIME_WASTING_OPERATIONS)
	private AbstractAggregatedTimedCallable<? extends TimedCallable> timeWastingOperation;

	@TagValue(type = RuleConstants.TAG_PROBLEM_CONTEXT)
	private TimedCallable problemContext;

	@SessionVariable(name = RuleConstants.VAR_BASELINE, optional = false)
	private double baseline;

	@Action(resultTag = RuleConstants.TAG_PROBLEM_CAUSE)
	public AbstractAggregatedTimedCallable<? extends TimedCallable> action() {
		log.info("Executing ProblemCauseRule..");

		// exclude callables that are not descendants of the context callable
		AbstractAggregatedTimedCallable<? extends TimedCallable> newAggregatedCallable = AbstractAggregatedTimedCallable
				.createAggregatedCallable(timeWastingOperation.getCallables().get(0), true);
		for (TimedCallable clbl : timeWastingOperation.getCallables()) {
			if (TraceUtils.isDescendantOf(problemContext, clbl)) {
				newAggregatedCallable.aggregate(clbl);
			}
		}
		// problemCauseTag.setCause(newAggregatedCallable);

		// ProblemInstance problemInstance = new ProblemInstance(problemContext,
		// newAggregatedCallable, (long) baseline);
		// // List<NewProblemTag> contextTags = new ArrayList<NewProblemTag>();
		// // System.out.println(problemInstance.getNodeType());
		// newAggregatedCallable.setProblemInstance(problemInstance);
		// contextTags.add(npTag);
		log.info("ProblemCauseRule: The type of the problem cause is: " + newAggregatedCallable.getType());

		return newAggregatedCallable;
	}
}