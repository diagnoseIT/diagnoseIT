//package org.diagnoseit.rules.mobile.impl;
//
//import org.diagnoseit.engine.rule.annotation.Action;
//import org.diagnoseit.engine.rule.annotation.Rule;
//import org.diagnoseit.engine.rule.annotation.TagValue;
//import org.diagnoseit.rules.RuleConstants;
//import org.spec.research.open.xtrace.api.core.MobileRemoteMeasurement;
//import org.spec.research.open.xtrace.api.core.callables.RemoteInvocation;
//
//@Rule(name = "LatencyRule")
//public class LatencyRule {
//
//	@TagValue(type = RuleConstants.TAG_REMOTE_INVOCATION)
//	private RemoteInvocation remoteInvocation;
//
//	@Action(resultTag = RuleConstants.TAG_LATENCY)
//	public boolean action() {
//
//		if (!remoteInvocation.getRequestMeasurement().isPresent()
//				|| !remoteInvocation.getRepsonseMeasurement().isPresent()) {
//			return false;
//		}
//		MobileRemoteMeasurement requestMeasurement = remoteInvocation
//				.getRequestMeasurement().get();
//		MobileRemoteMeasurement responseMeasurement = remoteInvocation
//				.getRepsonseMeasurement().get();
//
//		double longitudeFromRequest = requestMeasurement.getLongitude()
//				.isPresent() ? requestMeasurement.getLongitude().get() : -1;
//
//		double latitudeFromRequest = requestMeasurement.getLatitude()
//				.isPresent() ? requestMeasurement.getLatitude().get() : -1;
//
//		double longitudeFromResponse = responseMeasurement.getLongitude()
//				.isPresent() ? responseMeasurement.getLongitude().get() : -1;
//
//		double latitudeFromResponse = responseMeasurement.getLatitude()
//				.isPresent() ? responseMeasurement.getLatitude().get() : -1;
//
//		if (longitudeFromRequest == -1 || latitudeFromRequest == -1
//				|| longitudeFromResponse == -1 || latitudeFromResponse == -1) {
//			return false;
//		}
//
//		writeData(0, longitudeFromRequest, latitudeFromRequest,
//				requestMeasurement.getNetworkProvider().get(),
//				requestMeasurement.getNetworkConnection().get(),
//				remoteInvocation.getContainingSubTrace().getContainingTrace()
//						.getTraceId(), 0);
//
//		return true;
//	}
//
//	public void writeData(double latency, double longitude, double latitude,
//			String internetProvider, String networkConnection, long traceId,
//			long traceTimestamp) {
//		InfluxDBConnectorMobile influxDB = new InfluxDBConnectorMobile();
//		influxDB.connect();
//		influxDB.writeData(latency, longitude, latitude, internetProvider,
//				networkConnection, traceId, traceTimestamp);
//	}
// }