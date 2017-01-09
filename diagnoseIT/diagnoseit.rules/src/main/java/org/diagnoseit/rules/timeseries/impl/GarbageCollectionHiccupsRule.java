package org.diagnoseit.rules.timeseries.impl;

import java.util.LinkedList;
import java.util.List;

import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.engine.tag.Tags;
import org.diagnoseit.rules.RuleConstants;
import org.diagnoseit.rules.timeseries.impl.InfluxDBConnector.DataPointTraceID;

/**
 * Rule for detecting Garbage Collection Hiccups anti-pattern in timeseries data
 * 
 * @author Alper Hidiroglu
 *
 */
@Rule(name = "GarbageCollectionHiccupsRule")
public class GarbageCollectionHiccupsRule {

	/**
	 * Config parameter: GC time must be higher than response time multiplicated
	 * with this value, so that application hiccup is GC Hiccup
	 */
	private static final double GC_TIME_THRESHOLD = 0.5;

	@TagValue(type = Tags.ROOT_TAG)
	private InfluxDBConnector connector;

	/**
	 * fired after ApplicationHiccupsRule
	 */
	@TagValue(type = RuleConstants.TAG_APPLICATION_HICCUPS)
	private List<DataPointTraceID> appHiccupsDataPoints;

	/**
	 * Execution of the rule. When successful, this rule will return a GC hiccup tag
	 * 
	 * @return outlier data points with high gc times
	 */
	@Action(resultTag = RuleConstants.TAG_GARBAGE_COLLECTION_HICCUPS)
	public List<DataPointTraceID> action() {
		System.out.println("Executing GarbageCollectionHiccupsRule..");

		if (appHiccupsDataPoints.size() < 1) {
			System.out
					.println("GarbageCollectionHiccupsRule: Since there are no Application Hiccups, there also can't be Garbage Collection Hiccups.");
			return null;
		}

		List<DataPointTraceID> outliersCausedByGarbageCollector = new LinkedList<DataPointTraceID>();

		for (int i = 0; i < appHiccupsDataPoints.size(); i++) {
			double currentResponseTime = appHiccupsDataPoints.get(i)
					.getMeasurement1();
			double currentGCTime = appHiccupsDataPoints.get(i)
					.getMeasurement2();
			if (currentGCTime > currentResponseTime * GC_TIME_THRESHOLD) {
				outliersCausedByGarbageCollector.add(appHiccupsDataPoints
						.get(i));
			}
		}
		int amountAppHiccups = appHiccupsDataPoints.size();
		System.out.println("GarbageCollectionHiccupsRule: From " + amountAppHiccups
				+ " Application Hiccups, "
				+ outliersCausedByGarbageCollector.size()
				+ " are caused by the Garbage Collector.");
		return outliersCausedByGarbageCollector;
	}
}
