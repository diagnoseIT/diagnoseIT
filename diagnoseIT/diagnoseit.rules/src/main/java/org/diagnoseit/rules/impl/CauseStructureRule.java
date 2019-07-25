package org.diagnoseit.rules.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.rules.RuleConstants;
import org.diagnoseit.traceservices.TraceUtils;
import org.diagnoseit.traceservices.aggregated.AbstractAggregatedTimedCallable;
import org.diagnoseit.traceservices.result.CauseExecutionType;
import org.spec.research.open.xtrace.api.core.callables.TimedCallable;

/**
 * Cause Structure Rule investigates if methods are executed iterative or
 * recursive
 * 
 * @author Alexander Wert
 *
 */
@Rule(name = "CauseStructureRule")
public class CauseStructureRule {
	private static final int EXCESSIVE_CALLS_THRESHOLD = 1;

	@TagValue(type = RuleConstants.TAG_PROBLEM_CAUSE)
	private AbstractAggregatedTimedCallable<? extends TimedCallable> problemCause;

	private Logger log = Logger.getGlobal();

	@Action(resultTag = RuleConstants.TAG_CAUSE_STRUCTURE)
	public CauseExecutionType action() {
		log.info("Executing CauseStructureRule..");
		// ProblemInstance pInstance = problemCause.getProblemInstance();

		// System.out.println(pInstance.getCause());
		// List<CauseTypeTag> tags = new ArrayList<CauseTypeTag>();
		CauseExecutionType causeType = null;
		if (problemCause.getCount() == 1) {
			causeType = CauseExecutionType.SINGLE_CALL;
			log.info("CauseStructureRule: Structure of Problem Context: Single Call in Problem Context");
		} else if (problemCause.getCount() <= EXCESSIVE_CALLS_THRESHOLD) {
			causeType = CauseExecutionType.SOME_FEW_CALLS;
			log.info("CauseStructureRule: Structure of Problem Context: Just some few Calls in Problem Context");
		} else {
			// boolean recursive = false;
			List<TimedCallable> causeCallables = new ArrayList<TimedCallable>(problemCause.getCallables());
			causeCallables.sort(new Comparator<TimedCallable>() {

				@Override
				public int compare(TimedCallable o1, TimedCallable o2) {
					long diff = o2.getResponseTime() - o1.getResponseTime();
					if (diff > 0) {
						return 1;
					} else if (diff < 0) {
						return -1;
					} else {
						return 0;
					}

				}
			});
			// TODO: think about an efficient algorithm to detect recursiveness
			int ancestors = 0;
			int windowSize = 5;
			for (int i = windowSize; i < causeCallables.size(); i += windowSize) {
				for (int j = i - windowSize; j < i; j++) {
					TimedCallable current = causeCallables.get(i);
					TimedCallable potentialAncestor = causeCallables.get(j);
					if (TraceUtils.isDescendantOf(potentialAncestor, current)) {
						ancestors++;
						break;
					}
				}
			}

			if (ancestors > causeCallables.size() / 3) {
				causeType = CauseExecutionType.RECURSIVE;
				log.info("CauseStructureRule: Structure of Problem Cause: Recursive");
			} else if (ancestors == 0) {
				causeType = CauseExecutionType.ITERATIVE;
				log.info("CauseStructureRule: Structure of Problem Cause: Iterative");
			} else {
				causeType = CauseExecutionType.RECURSIVE_ITERATIVE_MIX;
				log.info("CauseStructureRule: Structure of Problem Cause: Recursive-Iterative-Mix");
			}

		}

		// tags.add(new CauseTypeTag(newProblemTag, causeType));
		// pInstance.setCauseType(causeType);
		return causeType;
	}
}
