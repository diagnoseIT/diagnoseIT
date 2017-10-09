//package org.diagnoseit.rules.mobile.impl;
//
//import org.diagnoseit.engine.rule.annotation.Action;
//import org.diagnoseit.engine.rule.annotation.Rule;
//import org.diagnoseit.engine.rule.annotation.TagValue;
//import org.diagnoseit.rules.RuleConstants;
//import org.spec.research.open.xtrace.api.core.MobileRemoteMeasurement;
//import org.spec.research.open.xtrace.api.core.SubTrace;
//import org.spec.research.open.xtrace.api.core.callables.RemoteInvocation;
//
///**
// * Rule detects high latency, although there is a good network connection.
// * 
// * @author Alper Hi
// *
// */
//@Rule(name = "HighLatencyRule")
//public class HighLatencyRule {
//
//	private static final int LATENCY_THRESHOLD = 10;
//
//	@TagValue(type = RuleConstants.TAG_REMOTE_INVOCATION)
//	private RemoteInvocation remoteInvocation;
//
//	/**
//	 * Rule execution.
//	 * @return
//	 */
//	@Action(resultTag = RuleConstants.TAG_LATENCY)
//	public long action() {
//
//		if (remoteInvocation.getTargetSubTrace().get() == null) {
//			return -1;
//		}
//
//		System.out.println("===== HighLatencyRule =====");
//
//		SubTrace subTrace = remoteInvocation.getTargetSubTrace().get();
//
//		long durationOfSubTrace = subTrace.getResponseTime();
//
//		System.out.println("Response Time from subTrace = "
//				+ durationOfSubTrace);
//
//		System.out.println("Exclusive Time from subTrace = "
//				+ subTrace.getExclusiveTime());
//
//		if (!remoteInvocation.getRequestMeasurement().isPresent()
//				|| !remoteInvocation.getResponseMeasurement().isPresent()) {
//			return -1;
//		}
//		MobileRemoteMeasurement requestMeasurement = remoteInvocation
//				.getRequestMeasurement().get();
//
//		MobileRemoteMeasurement responseMeasurement = remoteInvocation
//				.getResponseMeasurement().get();
//
//		long timestampOfRequest = requestMeasurement.getTimestamp().isPresent() ? requestMeasurement
//				.getTimestamp().get() : -1;
//
//		long timestampOfResponse = responseMeasurement.getTimestamp()
//				.isPresent() ? responseMeasurement.getTimestamp().get() : -1;
//
//		String networkConnection = requestMeasurement.getNetworkConnection()
//				.isPresent() ? requestMeasurement.getNetworkConnection().get()
//				: null;
//
//		if (timestampOfRequest == -1 || timestampOfResponse == -1
//				|| networkConnection == null) {
//			return -1;
//		}
//
//		long durationOfRemoteCall = timestampOfResponse - timestampOfRequest;
//
//		System.out.println("The duration of the complete remote call is = "
//				+ durationOfRemoteCall);
//
//		long latency = durationOfRemoteCall - durationOfSubTrace;
//
//		System.out.println("Latency is = " + latency);
//
//		return latency;
//
//	}
//}