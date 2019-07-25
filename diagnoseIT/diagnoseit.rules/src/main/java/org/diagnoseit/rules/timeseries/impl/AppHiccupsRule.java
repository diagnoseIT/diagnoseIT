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
 * Rule for detecting the Application Hiccups anti-pattern in timeseries data
 * 
 * @author Alper Hidiroglu
 *
 */
@Rule(name = "AppHiccupsRule")
public class AppHiccupsRule {

	// milli seconds
	private static final double THRESHOLD = 10000;

	@TagValue(type = Tags.ROOT_TAG)
	private InfluxDBConnector connector;

	/**
	 * Execution of the Rule
	 * 
	 * @return outlier data points
	 */
	@Action(resultTag = RuleConstants.TAG_APP_HICCUPS)
	public List<DataPointTraceID> action() {
		System.out.println("Executing AppHiccupsRule..");

		List<DataPointTraceID> dataPoints = new LinkedList<DataPointTraceID>();
		dataPoints = getDataPointsTraceID();

		List<DataPointTraceID> problematicDataPoints = new LinkedList<DataPointTraceID>();

		// the number of going up-down or down-up within the timeseries curve
		long numberOfCrossings = 0l;

		boolean isLastTimeAboveThreshold = (dataPoints.get(0).getMeasurement1() > THRESHOLD);

		for (int i = 1; i < dataPoints.size(); i++) {
			double currentTime = dataPoints.get(i).getMeasurement1();

			if (currentTime > THRESHOLD) {
				problematicDataPoints.add(dataPoints.get(i));
			}

			// down and up
			if ((isLastTimeAboveThreshold && currentTime < THRESHOLD)
					|| (!isLastTimeAboveThreshold && currentTime > THRESHOLD)) {
				numberOfCrossings++;
				isLastTimeAboveThreshold = !isLastTimeAboveThreshold;
			}
		}

		long actualHiccups = numberOfCrossings / 2;

		if (actualHiccups < 1) {
			System.out.println("AppHiccupsRule: No Application Hiccups detected.");
			return problematicDataPoints;
		}

		System.out.println("AppHiccupsRule: Amount of Application Hiccups: "
				+ actualHiccups + ".");
		return problematicDataPoints;
	}

	/**
	 * Fetches data points from time series database
	 * 
	 * @return data point with TraceIDs, response times, gc times (not needed)
	 */
	private List<DataPointTraceID> getDataPointsTraceID() {
		connector.connect();
		List<DataPointTraceID> listOfDataPoints = connector
				.getDataPointsWithTraceID();
		return listOfDataPoints;
	}
}
