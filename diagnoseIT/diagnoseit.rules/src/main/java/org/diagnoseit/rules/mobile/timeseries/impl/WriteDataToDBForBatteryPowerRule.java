//package org.diagnoseit.rules.mobile.timeseries.impl;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.diagnoseit.engine.rule.annotation.Action;
//import org.diagnoseit.engine.rule.annotation.Rule;
//import org.diagnoseit.engine.rule.annotation.TagValue;
//import org.diagnoseit.engine.tag.Tags;
//import org.diagnoseit.rules.RuleConstants;
//import org.spec.research.open.xtrace.api.core.Trace;
//import org.spec.research.open.xtrace.api.core.callables.Callable;
//import org.spec.research.open.xtrace.api.core.callables.MobileMetadataMeasurement;
//
//@Rule(name = "WriteDataToDBForBatteryPowerRule")
//public class WriteDataToDBForBatteryPowerRule {
//
//	@TagValue(type = Tags.ROOT_TAG)
//	private Trace trace;
//
//	// Connection
//	private InfluxDBConnectorMobile connector;
//
//	/**
//	 * Rule execution.
//	 * 
//	 * @return
//	 */
//	@Action(resultTag = RuleConstants.TAG_BATTERY_POWER)
//	public boolean action() {
//		
//		List<MobileMetadataMeasurement> mobileCallables = new ArrayList<MobileMetadataMeasurement>();
//
//		double sumCPUUsage = 0.0;
//		int amountOfCallablesWithCPUUsage = 0;
//
//		for (Callable callable : trace.getRoot()) {
//			if (callable instanceof MobileMetadataMeasurement) {
//				MobileMetadataMeasurement mobileCallable = (MobileMetadataMeasurement) callable;
//				if (mobileCallable.getBatteryPower().isPresent()) {
//					mobileCallables.add(mobileCallable);
//					if (mobileCallable.getCPUUsage().isPresent()) {
//						sumCPUUsage += mobileCallable.getCPUUsage().get();
//						amountOfCallablesWithCPUUsage++;
//					}
//				}
//			}
//		}
//
//		if (sumCPUUsage == 0.0) {
//			return false;
//		}
//
//		double averageCPUUsage = sumCPUUsage / amountOfCallablesWithCPUUsage;
//
//		double batteryPowerAtStart = mobileCallables.get(0).getBatteryPower()
//				.get();
//
//		long timestampAtStart = mobileCallables.get(0).getTimestamp();
//
//		double batteryPowerAtEnd = mobileCallables
//				.get(mobileCallables.size() - 1).getBatteryPower().get();
//
//		long timestampAtEnd = mobileCallables.get(mobileCallables.size() - 1)
//				.getTimestamp();
//
//		double batteryPowerDifference = batteryPowerAtEnd - batteryPowerAtStart;
//
//		long durationBetweenBatteryMeasurement = timestampAtEnd
//				- timestampAtStart;
//
//		System.out.println("BatteryPowerRule: Battery power difference = "
//				+ batteryPowerDifference + ". Time between the measurement = "
//				+ durationBetweenBatteryMeasurement);
//
//		return true;
//	}
//
//	private void writeData(long traceId, long measurementTimestamp,
//			String useCaseId, String useCaseName, double CPUUsage,
//			double memoryUsage, double batteryPower) {
//		connector.connect();
//		connector.writeData(traceId, measurementTimestamp, useCaseId,
//				useCaseName, CPUUsage, memoryUsage, batteryPower);
//	}
//
//	private void writeAdditionalDataForLastDataPoint(long traceId,
//			long measurementTimestamp, String useCaseId, String useCaseName,
//			double CPUUsage, double memoryUsage, double batteryPower,
//			double useCaseDuration, double averageCPUUsage,
//			double batteryPowerDifference) {
//		connector.connect();
//		connector.writeAdditionalData(traceId, measurementTimestamp, useCaseId,
//				useCaseName, CPUUsage, memoryUsage, batteryPower,
//				useCaseDuration, averageCPUUsage, batteryPowerDifference);
//	}
//}