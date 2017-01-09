package org.diagnoseit.rules.mobile.impl;

import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.engine.tag.Tags;
import org.diagnoseit.rules.RuleConstants;
import org.spec.research.open.xtrace.api.core.Trace;
import org.spec.research.open.xtrace.api.core.callables.Callable;
import org.spec.research.open.xtrace.dflt.impl.core.callables.RemoteInvocationImpl;

@Rule(name = "RemoteInvocationRule")
public class RemoteInvocationRule {

	private static final double REMOTE_CALLS_PERCENT = 0.07;

	@TagValue(type = Tags.ROOT_TAG)
	private Trace trace;

	@Action(resultTag = RuleConstants.TAG_REMOTE_INVOCATION)
	public boolean action() {

		int amountOfCallables = 0;
		int amountOfRemoteInvocations = 0;

		for (Callable callable : trace.getRoot()) {
			amountOfCallables++;
			if (callable instanceof RemoteInvocationImpl) {
				// RemoteInvocationImpl remoteInvo = (RemoteInvocationImpl)
				// callable;

				/**
				 * getTarget gibt zurück: Host, RuntimeEnvironment, Application,
				 * BusinessTransaction. Ist das selbe wie:
				 * targetSubTrace.getLocation().toString()
				 * getTarget().equal(....) vergleicht die vier Parameter von
				 * Location. Wenn die 4 Parameter von einer RemoteInvocation mit
				 * den 4 Parametern von einer anderen RemoteInvocation
				 * übereinstimmen, dann war der RemoteCall in beiden Fällen
				 * sozusagen der selbe (Anti-Pattern ?) -->
				 * remoteInvo.getTarget()
				 */

				amountOfRemoteInvocations++;
			}
		}

		System.out.println("Amount of Callables = " + amountOfCallables);
		System.out.println("Amount of RemoteInvocations = "
				+ amountOfRemoteInvocations);

		if (amountOfRemoteInvocations > amountOfCallables
				* REMOTE_CALLS_PERCENT) {
			System.out.println("Too many Remote Calls");
		}
		return true;
	}
}
