//package org.diagnoseit.rules.mobile.impl;
//
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.List;
//
//import org.diagnoseit.engine.rule.annotation.Action;
//import org.diagnoseit.engine.rule.annotation.Rule;
//import org.diagnoseit.engine.rule.annotation.TagValue;
//import org.diagnoseit.engine.tag.Tags;
//import org.diagnoseit.rules.RuleConstants;
//import org.spec.research.open.xtrace.api.core.SubTrace;
//import org.spec.research.open.xtrace.api.core.Trace;
//import org.spec.research.open.xtrace.api.core.callables.Callable;
//import org.spec.research.open.xtrace.api.core.callables.HTTPRequestProcessing;
//import org.spec.research.open.xtrace.api.core.callables.RemoteInvocation;
//
///**
// * Rule analyzes if the backend executed too many equal calls to the same URL.
// * 
// * @author Alper Hi
// *
// */
//@Rule(name = "BackendManyEqualURLCallsRule")
//public class BackendManyEqualURLCallsRule {
//
//	private static final double URL_CALLS_PERCENT = 0.03;
//
//	@TagValue(type = Tags.ROOT_TAG)
//	private Trace trace;
//
//	/**
//	 * Rule execution.
//	 * 
//	 * @return
//	 */
//	@Action(resultTag = RuleConstants.TAG_MANY_EQUAL_REMOTE_INVOCATIONS_BACKEND)
//	public boolean action() {
//
//		System.out.println("===== BackendManyEqualURLCallsRule =====");
//
//		List<SubTrace> javaAgentSubTraces = new LinkedList<SubTrace>();
//
//		for (Callable callable : trace.getRoot()) {
//			if (callable instanceof RemoteInvocation) {
//				RemoteInvocation remoteInvo = (RemoteInvocation) callable;
//				if (remoteInvo.getTargetSubTrace().isPresent()) {
//					javaAgentSubTraces
//							.add(remoteInvo.getTargetSubTrace().get());
//				}
//			}
//		}
//
//		List<SubTrace> javaSubTraces = new LinkedList<SubTrace>();
//
//		for (SubTrace subTrace : javaAgentSubTraces) {
//			for (Callable callable : subTrace) {
//				if (callable instanceof RemoteInvocation) {
//					RemoteInvocation remoteInvo = (RemoteInvocation) callable;
//					if (remoteInvo.getTargetSubTrace().isPresent()) {
//						javaSubTraces.add(remoteInvo.getTargetSubTrace()
//								.get());
//					}
//				}
//			}
//		}
//
//		int amountOfCallables = 0;
//		List<HTTPRequestProcessing> httpInvocations = new LinkedList<HTTPRequestProcessing>();
//
//		for (SubTrace subTrace : javaSubTraces) {
//			for (Callable callable : subTrace) {
//				amountOfCallables++;
//				if (callable instanceof HTTPRequestProcessing) {
//					HTTPRequestProcessing httpInvo = (HTTPRequestProcessing) callable;
//					httpInvocations.add(httpInvo);
//				}
//			}
//		}
//
//		if (httpInvocations.isEmpty()) {
//			return false;
//		}
//
//		/**
//		 * getTarget gibt zurück: Host, RuntimeEnvironment, Application,
//		 * BusinessTransaction. Ist das selbe wie:
//		 * targetSubTrace.getLocation().toString() getTarget().equal(....)
//		 * vergleicht die vier Parameter von Location. Wenn die 4 Parameter von
//		 * einer RemoteInvocation mit den 4 Parametern von einer anderen
//		 * RemoteInvocation übereinstimmen, dann war der RemoteCall in beiden
//		 * Fällen sozusagen der selbe (Anti-Pattern ?) -->
//		 * remoteInvo.getTarget()
//		 */
//
//		HashMap<String, Long> remoteInvoMap = new HashMap<String, Long>();
//
//		for (HTTPRequestProcessing httpInvo : httpInvocations) {
//			String url = httpInvo.getUri();
//
//			if (remoteInvoMap.containsKey(url)) {
//				remoteInvoMap.put(url, remoteInvoMap.get(url) + 1);
//			} else {
//				long amount = 1;
//				remoteInvoMap.put(url, amount);
//			}
//		}
//
//		boolean tooManyEqualURLCalls = false;
//
//		for (long amountEqualURLInvos : remoteInvoMap.values()) {
//			if (amountEqualURLInvos > amountOfCallables * URL_CALLS_PERCENT) {
//				System.out
//						.println("BackendManyEqualURLCallsRule: Java application executed too many equal URL calls. Amount = "
//								+ amountEqualURLInvos + ".");
//				tooManyEqualURLCalls = true;
//			}
//		}
//		return tooManyEqualURLCalls;
//	}
//}
