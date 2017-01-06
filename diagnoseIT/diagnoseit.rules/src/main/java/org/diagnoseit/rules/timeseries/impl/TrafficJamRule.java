package org.diagnoseit.rules.timeseries.impl;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.engine.tag.Tags;
import org.diagnoseit.rules.RuleConstants;
import org.diagnoseit.rules.timeseries.impl.InfluxDBConnector.DataPointTimestamp;

/**
 * Rule for detecting the Traffic Jam anti-pattern in time series data
 * 
 * @author Alper Hidiroglu
 *
 */
@Rule(name = "TrafficJamRule")
public class TrafficJamRule {

	/**
	 * Config parameter for coefficient of variation threshold
	 */
	private static final double COEFFICIENT_OF_VARIATION_THRESHOLD = 0.3;

	/**
	 * Config parameter to distinguish from the ramp anti-pattern. Slope of
	 * regression line should not be higher than this value
	 */
	private static final double SLOPE_THRESHOLD = 0.15;

	@TagValue(type = Tags.ROOT_TAG)
	// Connection
	private InfluxDBConnector connector;

	/**
	 * Execution of this rule. When successful, this rule produces a traffic jam
	 * tag
	 */
	@Action(resultTag = RuleConstants.TAG_TRAFFIC_JAM)
	public boolean action() {

		List<DataPointTimestamp> dataPoints = new LinkedList<DataPointTimestamp>();

		boolean trafficJamDetected = true;

		dataPoints = getDataPoints();

		if (dataPoints.size() <= 1) {
			return false;
		}

		// put regression line through data points
		SimpleRegression regression = new SimpleRegression(true);

		double firstTimestamp = dataPoints.get(0).getTimestamp();

		// decrease the range of timestamps, begin by zero
		for (int i = 0; i < dataPoints.size(); i++) {
			dataPoints.get(i).setTimestamp(
					dataPoints.get(i).getTimestamp() - firstTimestamp);
		}

		for (DataPointTimestamp dataPoint : dataPoints) {
			long responseTime = (long) dataPoint.getMeasurement(); // /1000000;
			long timestamp = (long) dataPoint.getTimestamp();
			// long timestampInSeconds =
			// TimeUnit.MILLISECONDS.toSeconds(timestamp);
			// regression.addData(timestampInSeconds, responseTime);
			regression.addData(timestamp, responseTime);
		}

		// the slope of the regression line has to be below the slope threshold
		if (Math.abs(regression.getSlope()) <= SLOPE_THRESHOLD) {
			
			double sumOfResponseTimes = 0;
			
			for(DataPointTimestamp dataPoint : dataPoints){
				sumOfResponseTimes += dataPoint.getMeasurement();
			}
			
			double meanResponseTime = sumOfResponseTimes / dataPoints.size();
			
			// calculates the variance of response times
			double tempVar = 0;
			for (DataPointTimestamp dataPoint : dataPoints)
				tempVar += (dataPoint.getMeasurement() - meanResponseTime)
						* (dataPoint.getMeasurement() - meanResponseTime);
			double variance = tempVar / (dataPoints.size() - 1);
			
			// Calculates standard deviation of response times
			double standardDeviation = Math.sqrt(variance);
			
			// Calculates coefficient of variation
			double coeffOfVariation = standardDeviation / meanResponseTime;

			System.out
					.println("Coefficient of variation = " + coeffOfVariation);

			// coefficient of variation has to be higher than threshold for
			// detecting traffic jam
			if (!(coeffOfVariation > COEFFICIENT_OF_VARIATION_THRESHOLD)) {
				trafficJamDetected = false;
			}
		} else {
			System.out.println("Slope is to high for Traffic Jam.");
			return false;
		}

		if (trafficJamDetected) {
			System.out.println("Variance of response times detected.");
		} else {
			System.out.println("No variance of response times detected.");
		}
		return trafficJamDetected;
	}

	/**
	 * Fetches data points with timestamp and response time from database
	 * 
	 * @return
	 */
	private List<DataPointTimestamp> getDataPoints() {
		connector.connect();
		List<DataPointTimestamp> listDataPoints = connector
				.getDataPointsWithTimestamp();
		return listDataPoints;
	}
	
	// The following methods are NOT needed

	/**
	 * Fetches mean of response times from database
	 * 
	 * @return
	 */
	private double getMeanResponseTime() {
		connector.connect();
		double meanResponseTime = connector.readAVGResponseTime();
		return meanResponseTime;
	}

	/**
	 * Fetches response times from database
	 * 
	 * @return
	 */
	private List<Double> getResponseTimes() {
		connector.connect();
		List<Double> responseTimes = connector.readResponseTimes();
		return responseTimes;
	}

	/**
	 * Calculates the variance of response times
	 * 
	 * @return
	 */
	private double getVarianceOfResponseTimes() {
		double meanResponseTime = getMeanResponseTime();
		List<Double> responseTimes = new LinkedList<Double>();
		responseTimes = getResponseTimes();
		double tempVar = 0;
		for (double responseTime : responseTimes)
			tempVar += (responseTime - meanResponseTime)
					* (responseTime - meanResponseTime);
		return tempVar / (responseTimes.size() - 1);
	}

	/**
	 * Calculates the standard deviation of response times
	 * 
	 * @return
	 */
	private double getStandardDeviationOfResponseTimes() {
		System.out.println("Standard Deviation of Response Times: "
				+ Math.sqrt(getVarianceOfResponseTimes()));
		return Math.sqrt(getVarianceOfResponseTimes());
	}

	/**
	 * Calculates the coefficient of variation
	 * 
	 * @return
	 */
	private double getCoefficientOfVariation() {
		double meanResponseTime = getMeanResponseTime();
		double standardDeviation = getStandardDeviationOfResponseTimes();
		return standardDeviation / meanResponseTime;
	}

}
