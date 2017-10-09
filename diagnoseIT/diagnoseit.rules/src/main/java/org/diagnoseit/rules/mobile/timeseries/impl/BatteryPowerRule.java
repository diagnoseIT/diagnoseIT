package org.diagnoseit.rules.mobile.timeseries.impl;

import java.util.List;

import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.engine.tag.Tags;
import org.diagnoseit.rules.RuleConstants;
import org.diagnoseit.rules.mobile.timeseries.impl.InfluxDBConnectorMobile.DataPointWithTimestampAndSixMeasurements;

/**
 * Rule analyzes...
 * 
 * @author Alper Hi
 *
 */
@Rule(name = "BatteryPowerRule")
public class BatteryPowerRule {

	@TagValue(type = Tags.ROOT_TAG)
	private InfluxDBConnectorMobile connector;

	/**
	 * 
	 * @return
	 */
	@Action(resultTag = RuleConstants.TAG_BATTERY_POWER)
	public boolean action() {

		System.out.println("===== BatteryPowerRule =====");

		List<DataPointWithTimestampAndSixMeasurements> dataPoints = getDataPoints();
		
		System.out.println("size of the list = " + dataPoints.size());
		//List<DataPointWithTimestamp>
		return true;

	}

	/**
	 * Fetches data points with timestamp, useCaseId, cpu usage, battery
	 * power, use case duration, average cpu time and battery power difference
	 * 
	 * @return
	 */
	private List<DataPointWithTimestampAndSixMeasurements> getDataPoints() {
		connector.connect();
		List<DataPointWithTimestampAndSixMeasurements> listDataPoints = connector
				.getDataPointsWithSevenMeasurements();
		return listDataPoints;
	}
}
