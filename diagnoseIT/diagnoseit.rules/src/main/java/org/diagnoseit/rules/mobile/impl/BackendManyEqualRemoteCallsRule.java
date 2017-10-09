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
//import org.spec.research.open.xtrace.api.core.callables.RemoteInvocation;
//
///**
// * Rule analyzes if the backend executed too many equal remote calls to the same
// * device that becomes a bottleneck.
// * 
// * @author Alper Hi
// *
// */
//@Rule(name = "BackendManyEqualRemoteCallsRule")
//public class BackendManyEqualRemoteCallsRule {
//
//	private static final double REMOTE_CALLS_PERCENT = 0.03;
//
//	@TagValue(type = Tags.ROOT_TAG)
//	private Trace trace;
//
//	/**
//	 * Rule execution.
//	 * @return
//	 */
//	@Action(resultTag = RuleConstants.TAG_MANY_EQUAL_REMOTE_INVOCATIONS_BACKEND)
//	public boolean action() {
//
//		System.out.println("===== BackendManyEqualRemoteCallsRule =====");
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
//		int amountOfCallables = 0;
//		List<RemoteInvocation> remoteInvocations = new LinkedList<RemoteInvocation>();
//
//		for (SubTrace subTrace : javaAgentSubTraces) {
//			for (Callable callable : subTrace) {
//				amountOfCallables++;
//				if (callable instanceof RemoteInvocation) {
//					RemoteInvocation remoteInvo = (RemoteInvocation) callable;
//					remoteInvocations.add(remoteInvo);
//				}
//			}
//		}
//
//		if (remoteInvocations.isEmpty()) {
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
//		for (RemoteInvocation remoteInvo : remoteInvocations) {
//			String remoteTarget = remoteInvo.getTarget();
//
//			if (remoteInvoMap.containsKey(remoteTarget)) {
//				remoteInvoMap.put(remoteTarget,
//						remoteInvoMap.get(remoteTarget) + 1);
//			} else {
//				long amount = 1;
//				remoteInvoMap.put(remoteTarget, amount);
//			}
//		}
//
//		boolean tooManyEqualRemoteCalls = false;
//
//		for (long amountEqualRemoteInvos : remoteInvoMap.values()) {
//			System.out
//					.println("BackendManyEqualRemoteCallsRule: The amount of equal remote calls the Java application executed is = "
//							+ amountEqualRemoteInvos + ".");
//			if (amountEqualRemoteInvos > amountOfCallables
//					* REMOTE_CALLS_PERCENT) {
//				System.out
//						.println("BackendManyEqualRemoteCallsRule: Java application executed too many equal remote calls.");
//				tooManyEqualRemoteCalls = true;
//			}
//		}
//		return tooManyEqualRemoteCalls;
//	}
//}
