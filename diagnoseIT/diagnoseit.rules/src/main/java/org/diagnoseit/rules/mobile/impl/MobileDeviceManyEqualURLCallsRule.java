package org.diagnoseit.rules.mobile.impl;

import java.util.HashMap;
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
import org.spec.research.open.xtrace.api.core.callables.HTTPRequestProcessing;
import org.spec.research.open.xtrace.api.core.callables.RemoteInvocation;

/**
 * Rule analyzes if the mobile device executed to many equal URL calls.
 * 
 * @author Alper Hi
 *
 */
@Rule(name = "MobileDeviceManyEqualURLCallsRule")
public class MobileDeviceManyEqualURLCallsRule {

	private static final double REMOTE_CALLS_PERCENT = 0.03;

	@TagValue(type = Tags.ROOT_TAG)
	private Trace trace;

	/**
	 * Rule execution.
	 * 
	 * @return
	 */
	@Action(resultTag = RuleConstants.TAG_MANY_EQUAL_URL_CALLS_MOBILE)
	public boolean action() {

		System.out.println("===== MobileDeviceManyEqualURLCallsRule =====");

		List<SubTrace> javaAgentSubTraces = new LinkedList<SubTrace>();
		int amountOfCallables = 0;

		for (Callable callable : trace.getRoot()) {
			amountOfCallables++;
			if (callable instanceof RemoteInvocation) {
				RemoteInvocation remoteInvo = (RemoteInvocation) callable;
				if (remoteInvo.getTargetSubTrace().isPresent()) {
					javaAgentSubTraces
							.add(remoteInvo.getTargetSubTrace().get());
				}
			}
		}

		if (javaAgentSubTraces.isEmpty()) {
			return false;
		}

		List<HTTPRequestProcessing> httpRequests = new LinkedList<HTTPRequestProcessing>();

		for (SubTrace subtrace : javaAgentSubTraces) {
			if (subtrace.getRoot() instanceof HTTPRequestProcessing) {
				HTTPRequestProcessing currentRequest = (HTTPRequestProcessing) subtrace
						.getRoot();
				httpRequests.add(currentRequest);
			}
		}

		HashMap<String, Long> requestMap = new HashMap<String, Long>();

		for (HTTPRequestProcessing httpRequest : httpRequests) {
			String url = httpRequest.getUri();

			if (requestMap.containsKey(url)) {
				requestMap.put(url, requestMap.get(url) + 1);
			} else {
				long amount = 1;
				requestMap.put(url, amount);
			}
		}

		boolean tooManyEqualHTTPRequests = false;

		for (long amountEqualHTTPRequest : requestMap.values()) {
			if (amountEqualHTTPRequest > amountOfCallables
					* REMOTE_CALLS_PERCENT) {
				System.out
						.println("MobileDeviceManyEqualURLCallsRule: Mobile application executed too many equal URL calls. Amount = "
								+ amountEqualHTTPRequest + ".");
				// return true;
				tooManyEqualHTTPRequests = true;
			}
		}
		// return false;
		return tooManyEqualHTTPRequests;
	}
}