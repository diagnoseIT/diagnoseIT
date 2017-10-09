package org.diagnoseit.rules.mobile.impl;

/**
 * Rule checks whether on the client side there was a timeout. It checks further
 * if the response would have been received.
 * 
 * @author Alper Hi
 *
 */
//@Rule(name = "ResponseTimeoutRule")
//public class ResponseTimeoutRule {
//
//	@TagValue(type = Tags.ROOT_TAG)
//	private Trace trace;
//
//	/**
//	 * Rule execution.
//	 * @return
//	 */
//	@Action(resultTag = RuleConstants.TAG_RESPONSE_TIMEOUT)
//	public boolean action() {
//		
//		System.out.println("===== ResponseTimeoutRule =====");
//
//		for (Callable callable : trace.getRoot()) {
//			if (!(callable instanceof RemoteInvocation)) {
//				continue;
//			}
//			RemoteInvocation remoteInvo = (RemoteInvocation) callable;
//			if (remoteInvo.getResponseMeasurement().isPresent()) {
//				MobileRemoteMeasurement mobileRemoteMeasurement = remoteInvo
//						.getResponseMeasurement().get();
//
//				if (mobileRemoteMeasurement.getTimeout().isPresent()) {
//					boolean isTimeout = mobileRemoteMeasurement.getTimeout()
//							.get();
//					if (isTimeout && remoteInvo.getTargetSubTrace().isPresent()) {
//						System.out
//								.println("ResponseTimeoutRule: Timeout on mobile client and the response did come too late.");
//						return true;
//					}
//				}
//			}
//		}
//		return false;
//	}
//}
