package org.diagnoseit.rules.timeseries.impl;
//
//import org.diagnoseit.engine.rule.annotation.Action;
//import org.diagnoseit.engine.rule.annotation.Rule;
//import org.diagnoseit.engine.rule.annotation.TagValue;
//import org.diagnoseit.engine.tag.Tags;
//import org.diagnoseit.rules.RuleConstants;
//import org.spec.research.open.xtrace.api.core.Trace;
//
//@Rule(name = "SaveTraceRule")
public class SaveTraceRule {
//
//	@TagValue(type = Tags.ROOT_TAG)
//	private Trace trace;
//
//	@Action(resultTag = RuleConstants.TAG_TRIGGER_TIMESERIES_RULES)
//	public boolean action() {
//
//		// List<String> traceIds = new LinkedList<String>();
//		double responseTime = trace.getResponseTime();
//		long traceId = trace.getTraceId();
//		long timestamp = trace.getRoot().getRoot().getTimestamp();
//
//		InfluxDBConnector connector = new InfluxDBConnector();
//		connector.connect();
//
//		// traceIds = connector.getTraceIds();
//
//		
//		connector.writeData(responseTime, 0, traceId, timestamp);
//
//		// if (!traceIds.contains(traceId)) {
//		// connector.writeData(responseTime, traceId,
//		// System.currentTimeMillis());
//		// }
//
//		return true;
//
//	}
}
