package org.diagnoseit.rules.timeseries.impl;

import java.util.ArrayList;
import java.util.List;

import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.engine.tag.Tags;
import org.diagnoseit.rules.RuleConstants;
import org.diagnoseit.rules.timeseries.impl.InfluxDBConnector.DataPointTwoMeasurements;

@Rule(name = "MoreIsLessRule")
public class MoreIsLessRule {

	private static final double RESPONSE_TIME_PERCENTAGE = 0.4;
	private static final double CPU_TIME_PERCENTAGE = 0.5;

	@TagValue(type = Tags.ROOT_TAG)
	// Connection
	private InfluxDBConnector connector;

	@Action(resultTag = RuleConstants.TAG_MORE_IS_LESS)
	public boolean action() {
		System.out.println("Executing MoreIsLessRule..");

		List<DataPointTwoMeasurements> dataPoints = new ArrayList<DataPointTwoMeasurements>();

		dataPoints = getDataPointsResponseAndCPUTimes();

		if (dataPoints.size() <= 1) {
			return false;
		}
		double sumOfResponseTimes = 0;
		double sumOfCPUTimes = 0;

		for (DataPointTwoMeasurements dataPoint : dataPoints) {
			sumOfResponseTimes += dataPoint.getMeasurementTwo();
			sumOfCPUTimes += dataPoint.getMeasurementOne();
		}
		double avgResponseTime = sumOfResponseTimes / dataPoints.size();
		double avgCPUTime = sumOfCPUTimes / dataPoints.size();

		// System.out.println("avgResponseTime " + avgResponseTime);
		// System.out.println("avgCPUTime " + avgCPUTime);

		for (int i = 0; i < dataPoints.size(); i++) {
			DataPointTwoMeasurements currentObject = dataPoints.get(i);
			if (currentObject.getMeasurementTwo() * RESPONSE_TIME_PERCENTAGE > avgResponseTime) {
				double currentObjectCPUPercentage = currentObject
						.getMeasurementOne()
						/ currentObject.getMeasurementTwo();
				double avgCPUPercentage = avgCPUTime / avgResponseTime;
				if (currentObjectCPUPercentage < avgCPUPercentage
						* CPU_TIME_PERCENTAGE) {
					// System.out.println("response time current object "
					// + currentObject.getMeasurementTwo());
					// System.out.println("cpu time current object "
					// + currentObject.getMeasurementOne());
					// System.out.println("current object cpu percentage "
					// + currentObjectCPUPercentage);
					// System.out.println("cpu percentage of system "
					// + avgCPUPercentage * CPU_TIME_PERCENTAGE);
					System.out.println("MoreIsLessRule: More is Less anti-pattern detected.");
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Gets Trace information from timeseries database
	 * 
	 * @return
	 */
	private List<DataPointTwoMeasurements> getDataPointsResponseAndCPUTimes() {
		connector.connect();
		List<DataPointTwoMeasurements> dataPoints = connector
				.getDataPointsResponseAndCPUTimes();
		return dataPoints;
	}
}
