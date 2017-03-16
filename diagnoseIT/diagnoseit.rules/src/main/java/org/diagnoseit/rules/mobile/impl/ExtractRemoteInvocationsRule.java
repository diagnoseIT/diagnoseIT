package org.diagnoseit.rules.mobile.impl;

import java.util.ArrayList;
import java.util.List;

import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.engine.tag.Tags;
import org.diagnoseit.rules.RuleConstants;
import org.spec.research.open.xtrace.api.core.Trace;
import org.spec.research.open.xtrace.api.core.callables.Callable;
import org.spec.research.open.xtrace.api.core.callables.RemoteInvocation;

/**
 * Rule extracts remote invocations from mobile trace.
 * @author Alper Hi
 *
 */
@Rule(name = "ExtractRemoteInvocationsRule")
public class ExtractRemoteInvocationsRule {

	@TagValue(type = Tags.ROOT_TAG)
	private Trace trace;

	/**
	 * Execution of the rule. Possibly returns more than one remote invocation.
	 * @return
	 */
	@Action(resultTag = RuleConstants.TAG_REMOTE_INVOCATION, resultQuantity = Action.Quantity.MULTIPLE)
	public List<RemoteInvocation> action() {

		List<RemoteInvocation> remoteInvocations = new ArrayList<RemoteInvocation>();

		for (Callable callable : trace.getRoot()) {
			if (callable instanceof RemoteInvocation) {
				RemoteInvocation remoteInvo = (RemoteInvocation) callable;
				remoteInvocations.add(remoteInvo);
			}
		}
		return remoteInvocations;

	}
}
