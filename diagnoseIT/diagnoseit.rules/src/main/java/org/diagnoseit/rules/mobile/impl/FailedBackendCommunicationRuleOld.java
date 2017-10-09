//package org.diagnoseit.rules.mobile.impl;
//
//import java.util.ArrayList;
//import java.util.Iterator;
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
//import org.spec.research.open.xtrace.api.core.callables.MobileMetadataMeasurement;
//import org.spec.research.open.xtrace.api.core.callables.RemoteInvocation;
//
//@Rule(name = "FailedBackendCommunicationRule")
//public class FailedBackendCommunicationRuleOld {
//
//	@TagValue(type = Tags.ROOT_TAG)
//	private Trace trace;
//
//	@Action(resultTag = RuleConstants.TAG_FAILED_BACKEND_COMMUNICATION, resultQuantity = Action.Quantity.MULTIPLE)
//	public List<RemoteInvocation> action() {
//
//		System.out.println("Executing FailedBackendCommunicationRule...");
//		List<Callable> listOfCallables = new ArrayList<Callable>();
//
//		for (Callable callable : trace.getRoot()) {
//			listOfCallables.add(callable);
//		}
//
//		Iterator<Callable> callableIterator = listOfCallables.iterator();
//
//		MobileMetadataMeasurement lastMobileMetadataMeasurement = null;
//
//		List<RemoteInvocation> failedBackendCalls = new LinkedList<RemoteInvocation>();
//
//		while (callableIterator.hasNext()) {
//			Callable current = callableIterator.next();
//			if (current instanceof RemoteInvocation) {
//					RemoteInvocation currentRemoteInvo = (RemoteInvocation) current;
//					if (currentRemoteInvo.getTargetSubTrace().isPresent()) {
//						SubTrace targetSubTrace = currentRemoteInvo
//								.getTargetSubTrace().get();
//						Callable rootOfSubTrace = targetSubTrace.getRoot();
//						if (rootOfSubTrace instanceof HTTPRequestProcessing) {
//							HTTPRequestProcessing hrp = (HTTPRequestProcessing) rootOfSubTrace;
//							if (hrp.getResponseCode().isPresent()) {
//								long responseCode = hrp.getResponseCode().get();
//								if (responseCode != 200
//										&& lastMobileMetadataMeasurement != null) {
//									failedBackendCalls.add(currentRemoteInvo);
//									if (lastMobileMetadataMeasurement
//											.getNetworkConnection().isPresent()) {
//										if (lastMobileMetadataMeasurement
//												.getNetworkConnection().get() == null) {
//											System.out
//													.println("FailedBackendCommunicationRule: Failed backend call due to missing internet connection. Target information: "
//															+ currentRemoteInvo
//																	.getTarget());
//										} else {
//											System.out
//													.println("FailedBackendCommunicationRule: Failed backend call, but not due to missing internet connection. Target information: "
//															+ currentRemoteInvo
//																	.getTarget());
//										}
//									} else {
//										System.out
//												.println("FailedBackendCommunicationRule: Failed backend call, but no further information available. Target information: "
//														+ currentRemoteInvo
//																.getTarget());
//									}
//
//								} else if (responseCode != 200) {
//									failedBackendCalls.add(currentRemoteInvo);
//									System.out
//											.println("FailedBackendCommunicationRule: Failed backend call, but no further information available. Target information: "
//													+ currentRemoteInvo
//															.getTarget());
//								}
//							}
//						}
//					}
//			} else if (current instanceof MobileMetadataMeasurement) {
//				lastMobileMetadataMeasurement = (MobileMetadataMeasurement) current;
//			}
//		}
//		return failedBackendCalls;
//
//	}
//}
