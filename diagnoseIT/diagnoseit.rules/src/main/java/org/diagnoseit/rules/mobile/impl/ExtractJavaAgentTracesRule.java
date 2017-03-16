package org.diagnoseit.rules.mobile.impl;

import java.util.LinkedList;
import java.util.List;

import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.engine.tag.Tags;
import org.diagnoseit.rules.RuleConstants;
import org.spec.research.open.xtrace.api.core.SubTrace;
import org.spec.research.open.xtrace.api.core.Trace;
import org.spec.research.open.xtrace.api.core.callables.Callable;
import org.spec.research.open.xtrace.api.core.callables.RemoteInvocation;

/**
 * Rule extracts Java agent subtraces from mobile trace.
 * @author Alper Hi
 *
 */
@Rule(name = "ExtractJavaAgentTracesRule")
public class ExtractJavaAgentTracesRule {

	@TagValue(type = Tags.ROOT_TAG)
	private Trace trace;

	/**
	 * Execution of the rule. Possibly returns more than one Java agent subtrace.
	 * @return
	 */
	@Action(resultTag = RuleConstants.TAG_JAVA_AGENT_SUBTRACE, resultQuantity = Action.Quantity.MULTIPLE)
	public List<SubTrace> action() {

		List<SubTrace> javaAgentSubTraces = new LinkedList<SubTrace>();

		for (Callable callable : trace.getRoot()) {
			if (callable instanceof RemoteInvocation) {
				RemoteInvocation remoteInvo = (RemoteInvocation) callable;
				if (remoteInvo.getTargetSubTrace().get() != null) {
					javaAgentSubTraces
							.add(remoteInvo.getTargetSubTrace().get());
				}
			}
		}
		return javaAgentSubTraces;
	}
}
