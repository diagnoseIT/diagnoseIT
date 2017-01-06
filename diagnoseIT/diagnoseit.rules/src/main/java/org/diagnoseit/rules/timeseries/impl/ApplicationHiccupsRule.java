package org.diagnoseit.rules.timeseries.impl;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;
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
@Rule(name = "ApplicationHiccupsRule")
public class ApplicationHiccupsRule {

	@TagValue(type = Tags.ROOT_TAG)
	private InfluxDBConnector connector;

	/**
	 * Execution of the rule. The rule adds to problematic data points a tag
	 * 
	 * @return outlier data points with high response times
	 */
	@Action(resultTag = RuleConstants.TAG_APPLICATION_HICCUPS)
	public List<DataPointTraceID> action() {

		List<DataPointTraceID> dataPoints = new LinkedList<DataPointTraceID>();
		dataPoints = getDataPointsTraceID();

		List<DataPointTraceID> problematicDataPoints = new LinkedList<DataPointTraceID>();

		if (dataPoints.size() < 3) {
			return problematicDataPoints;
		}

		double[] responseTimesArray = new double[dataPoints.size()];
		for (int i = 0; i < dataPoints.size(); i++)
			responseTimesArray[i] = dataPoints.get(i).getMeasurement1();

		Arrays.sort(responseTimesArray);
		// double maxValue = responseTimesArray[responseTimesArray.length - 1];

		double lowerQuartile = new Percentile()
				.evaluate(responseTimesArray, 25);

		double upperQuartile = new Percentile()
				.evaluate(responseTimesArray, 75);

		double interquartileRange = upperQuartile - lowerQuartile;

		// double lowerWhisker = (interquartileRange * 1.5) -
		// interquartileRange;

		double upperWhiskerThreshold = upperQuartile + interquartileRange
				* 1.5;
		System.out.println("upper whisker threshold: " + upperWhiskerThreshold);

		for (int i = 0; i < dataPoints.size(); i++) {
			if (dataPoints.get(i).getMeasurement1() > upperWhiskerThreshold) {
				problematicDataPoints.add(dataPoints.get(i));
			}
		}

		for (int i = 0; i < problematicDataPoints.size(); i++) {
			System.out.println("Problematic Data Points: " + "response time: "
					+ problematicDataPoints.get(i).getMeasurement1() + " ID: "
					+ problematicDataPoints.get(i).getTraceId());
		}

		if (problematicDataPoints.size() < 1) {
			System.out.println("No Application Hiccups detected.");
			return problematicDataPoints;
		}

		System.out.println("Amount of Application Hiccups: "
				+ problematicDataPoints.size() + ".");
		return problematicDataPoints;
	}

	/**
	 * Fetches data points from time series database
	 * 
	 * @return data point with TraceIDs, response times, gc times
	 */
	private List<DataPointTraceID> getDataPointsTraceID() {
		connector.connect();
		List<DataPointTraceID> listOfDataPoints = connector
				.getDataPointsWithTraceID();
		return listOfDataPoints;
	}

}
