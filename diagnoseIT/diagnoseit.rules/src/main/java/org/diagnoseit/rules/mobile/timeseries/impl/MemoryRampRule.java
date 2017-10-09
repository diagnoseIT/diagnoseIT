//package org.diagnoseit.rules.mobile.timeseries.impl;
//
//import java.util.LinkedList;
//import java.util.List;
//
//import org.apache.commons.math3.stat.regression.SimpleRegression;
//import org.diagnoseit.engine.rule.annotation.Action;
//import org.diagnoseit.engine.rule.annotation.Rule;
//import org.diagnoseit.engine.rule.annotation.TagValue;
//import org.diagnoseit.engine.tag.Tags;
//import org.diagnoseit.rules.RuleConstants;
//import org.diagnoseit.rules.mobile.timeseries.impl.InfluxDBConnectorMobile.DataPointTimestamp;
//
///**
// * The memory ramp can be a Dormant References anti-pattern.
// * 
// * @author Alper Hi
// *
// */
//@Rule(name = "MemoryRampRule")
//public class MemoryRampRule {
//
//	/**
//	 * Config parameter: slope of the regression line has to be higher than this
//	 * value
//	 */
//	private static final double SLOPE_THRESHOLD = 0.15;
//
//	@TagValue(type = Tags.ROOT_TAG)
//	// Connection
//	private InfluxDBConnectorMobile connector;
//
//	/**
//	 * Execution of the rule. When successful, this rule produces a ramp tag.
//	 * 
//	 * @return
//	 */
//	@Action(resultTag = RuleConstants.TAG_THE_RAMP)
//	public boolean action() {
//		System.out.println("Executing MemoryRampRule..");
//
//		List<DataPointTimestamp> dataPoints = new LinkedList<DataPointTimestamp>();
//
//		boolean rampDetected = true;
//
//		dataPoints = getDataPoints();
//
//		if (dataPoints.size() <= 1) {
//			return false;
//		}
//
//		// put regression line through data points
//		SimpleRegression regression = new SimpleRegression();
//
//		double firstTimestamp = dataPoints.get(0).getTimestamp();
//
//		// decrease the range of timestamps, begin by zero
//		for (int i = 0; i < dataPoints.size(); i++) {
//			dataPoints.get(i).setTimestamp(
//					dataPoints.get(i).getTimestamp() - firstTimestamp);
//		}
//
//		for (DataPointTimestamp dataPoint : dataPoints) {
//			double memoryUsage = dataPoint.getMeasurement(); // / 1000000;
//			long timestamp = (long) dataPoint.getTimestamp();
//
//			regression.addData(timestamp, memoryUsage);
//		}
//
//		System.out.println("TheRampRule: Slope of the regression line = "
//				+ regression.getSlope());
//
//		// the slope through the data points has to be equal or higher than the
//		// threshold to detect the ramp
//		if (!(regression.getSlope() > SLOPE_THRESHOLD)) {
//			rampDetected = false;
//		}
//
//		if (rampDetected) {
//			System.out.println("MemoryRampRule: Memory usage rises over time.");
//		} else {
//			System.out
//					.println("MemoryRampRule: No rise of memory usage detected.");
//		}
//		return rampDetected;
//	}
//
//	/**
//	 * Fetches data points with timestamp and memory usage from the database
//	 * 
//	 * @return
//	 */
//	private List<DataPointTimestamp> getDataPoints() {
//		connector.connect();
//		List<DataPointTimestamp> listDataPoints = connector
//				.getDataPointsWithTimestamp();
//		return listDataPoints;
//	}
//
//}
