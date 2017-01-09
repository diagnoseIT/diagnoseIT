package org.diagnoseit.rules.impl;

import java.util.LinkedList;
import java.util.List;

import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.SessionVariable;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.engine.tag.Tags;
import org.diagnoseit.rules.RuleConstants;
import org.spec.research.open.xtrace.api.core.Trace;
import org.spec.research.open.xtrace.api.core.callables.Callable;
import org.spec.research.open.xtrace.api.core.callables.MethodInvocation;
import org.spec.research.open.xtrace.api.core.callables.TimedCallable;

/**
 * Rule for detecting an expensive computation (CPU bound slowest method) within
 * a trace
 * 
 * @author Alper Hi
 *
 */
@Rule(name = "ExpensiveComputationRule")
public class ExpensiveComputationRule {
	private static final double CPU_PROCENT_REFERENCE_VALUE = 0.10;

	@TagValue(type = Tags.ROOT_TAG)
	private Trace trace;

	@SessionVariable(name = RuleConstants.VAR_BASELINE, optional = false)
	private double baseline;

	/**
	 * 
	 * @return
	 */
	@Action(resultTag = RuleConstants.TAG_EXPENSIVE_COMPUTATION)
	public TimedCallable action() {
		System.out.println("Executing ExpensiveComputationRule..");
		long traceResponseTime = trace.getResponseTime() / 1000000;
		MethodInvocation methodInvoHighestCPUTime = null;
		long highestExclusiveCPUTime = 0l;
		List<MethodInvocation> methodInvocations = new LinkedList<MethodInvocation>();

		if (traceResponseTime >= baseline) {
			for (Callable callable : trace) {
				if (callable instanceof MethodInvocation) {
					MethodInvocation methodInvo = (MethodInvocation) callable;
					if (methodInvo.getCPUTime().isPresent()
							|| methodInvo.getExclusiveCPUTime().isPresent()) {
						methodInvocations.add(methodInvo);
					}
				}
			}
			if (methodInvocations.size() >= 1) {
				for (int i = 0; i < methodInvocations.size(); i++) {
					if (methodInvocations.get(i).getExclusiveCPUTime().get() > highestExclusiveCPUTime) {
						highestExclusiveCPUTime = methodInvocations.get(i)
								.getExclusiveCPUTime().get();
						methodInvoHighestCPUTime = methodInvocations.get(i);
					}
				}
				double CPUPercentage = ((double) methodInvoHighestCPUTime
						.getExclusiveCPUTime().get() / 1000000)
						/ ((double) traceResponseTime);

				if (CPUPercentage > CPU_PROCENT_REFERENCE_VALUE) {
					System.out.println("ExpensiveComputationRule: There is an expensive computation"
							+ methodInvoHighestCPUTime);
					return methodInvoHighestCPUTime;
				}
			}
		}

		System.out
				.println("ExpensiveComputationRule: Trace response time is to low or there is no expensive computation");
		return null;
	}
}
