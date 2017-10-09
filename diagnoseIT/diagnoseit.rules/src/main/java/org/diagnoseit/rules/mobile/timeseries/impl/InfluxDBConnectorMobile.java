package org.diagnoseit.rules.mobile.timeseries.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;

/**
 * Connection to InfluxDB for diagnoseIT time series analysis
 * 
 * @author Alper Hi
 *
 */
public class InfluxDBConnectorMobile {

	/**
	 * Trace database is used for diagnoseIT time series analysis
	 */
	private static final String DATABASE_NAME = "trace";
	private static final String DATABASE_URL = "http://127.0.0.1:8086";
	private static final String DATABASE_USERNAME = "Alper";
	private static final String DATABASE_PASSWORD = "Alper";

	private static final String INFLUXDB_TAG_TRACE = "TraceID";

	/**
	 * Mobile data database table
	 */
	private static final String TABLE_MOBILE_DATA = "MobileData";
	private static final String USE_CASE_ID = "use_case_id";
	private static final String USE_CASE_NAME = "use_case_name";
	private static final String CPU_USAGE = "cpu_usage";
	private static final String MEMORY_USAGE = "memory_usage";
	private static final String BATTERY_POWER = "battery_power";
	private static final String USE_CASE_DURATION = "use_case_duration";
	private static final String AVG_CPU_USAGE = "avg_cpu_usage";
	private static final String BATTERY_POWER_DIFFERENCE = "battery_power_difference";

	private static final String RETENTION_POLICY = "autogen";

	private InfluxDB influxDB = null;

	/**
	 * Main method for testing
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		// Thread timeThread = new TimeseriesThread();
		// timeThread.start();
		InfluxDBConnectorMobile test = new InfluxDBConnectorMobile();
		test.connect();

		test.writeData(0, System.currentTimeMillis(), "", "", 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0);
	}

	/**
	 * connection to the time series database
	 */
	public void connect() {
		influxDB = InfluxDBFactory.connect(DATABASE_URL, DATABASE_USERNAME,
				DATABASE_PASSWORD);
	}

	/**
	 * read all data from timing information database table
	 */
	public void readData() {

		Query query = new Query("SELECT * FROM " + TABLE_MOBILE_DATA,
				DATABASE_NAME);
		QueryResult qResult = influxDB.query(query);

		for (Result result : qResult.getResults()) {
			System.out.println(result.getError());
			System.out.println(result);
			for (Series serie : result.getSeries()) {
				System.out.println(serie);
			}
		}
	}

	/**
	 * Writes a data point to the mobile data database table
	 * 
	 * @param responseTime
	 * @param cpuTime
	 * @param gcTime
	 * @param traceId
	 * @param measurementTimestamp
	 */
	public void writeData(long traceId, long measurementTimestamp,
			String useCaseId, String useCaseName, double CPUUsage,
			double memoryUsage, double batteryPower) {
		Point point1 = Point.measurement(TABLE_MOBILE_DATA)
				.tag(INFLUXDB_TAG_TRACE, String.valueOf(traceId))
				.time(measurementTimestamp, TimeUnit.MILLISECONDS)
				.field(USE_CASE_ID, useCaseId)
				.field(USE_CASE_NAME, useCaseName)
				.field(CPU_USAGE, CPUUsage)
				.field(MEMORY_USAGE, memoryUsage)
				.field(BATTERY_POWER, batteryPower).build();

		influxDB.write(DATABASE_NAME, RETENTION_POLICY, point1);
	}

	/**
	 * Writes additional data for last measurement point of a use case to sum up
	 * information of that use case
	 * 
	 * @param traceId
	 * @param measurementTimestamp
	 * @param useCaseId
	 * @param useCaseName
	 * @param CPUUsage
	 * @param memoryUsage
	 * @param batteryPower
	 * @param useCaseDuration
	 * @param averageCPUUsage
	 * @param batteryPowerDifference
	 */
	public void writeData(long traceId, long measurementTimestamp,
			String useCaseId, String useCaseName, double CPUUsage,
			double memoryUsage, double batteryPower, double useCaseDuration,
			double averageCPUUsage, double batteryPowerDifference) {
		Point point1 = Point.measurement(TABLE_MOBILE_DATA)
				.tag(INFLUXDB_TAG_TRACE, String.valueOf(traceId))
				.time(measurementTimestamp, TimeUnit.MILLISECONDS)
				.field(USE_CASE_ID, useCaseId)
				.field(USE_CASE_NAME, useCaseName)
				.field(CPU_USAGE, CPUUsage)
				.field(MEMORY_USAGE, memoryUsage)
				.field(BATTERY_POWER, batteryPower)
				.field(USE_CASE_DURATION, useCaseDuration)
				.field(AVG_CPU_USAGE, averageCPUUsage)
				.field(BATTERY_POWER_DIFFERENCE, batteryPower).build();

		influxDB.write(DATABASE_NAME, RETENTION_POLICY, point1);
	}

	/**
	 * Fetches timestamp and memory usage from timing information database table
	 * and puts them into objects
	 * 
	 * @return
	 */
	public List<DataPointTimestamp> getDataPointsWithTimestamp() {
		List<DataPointTimestamp> dataPoints = new LinkedList<DataPointTimestamp>();
		String command = "select time, memory_usage from %s where time >= '2017-01-01T00:00:00Z' "
				+ "AND time <= now() fill(none)";
		command = String.format(command, TABLE_MOBILE_DATA);

		Query query = new Query(command, DATABASE_NAME);

		QueryResult qResult = influxDB.query(query);

		DataPointTimestamp point;

		for (Result result : qResult.getResults()) {
			for (Series serie : result.getSeries()) {
				for (List<Object> listTuples : serie.getValues()) {
					DateFormat dateFormat = new SimpleDateFormat(
							"yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
					DateFormat dateFormat1 = new SimpleDateFormat(
							"yyyy-MM-dd'T'HH:mm:ss.SSXXX");
					DateFormat dateFormat2 = new SimpleDateFormat(
							"yyyy-MM-dd'T'HH:mm:ss.SXXX");
					DateFormat dateFormat3 = new SimpleDateFormat(
							"yyyy-MM-dd'T'HH:mm:SSSXXX");
					long timestamp = 0;
					try {
						timestamp = dateFormat
								.parse((String) listTuples.get(0)).getTime();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						try {
							timestamp = dateFormat1.parse(
									(String) listTuples.get(0)).getTime();
						} catch (ParseException e1) {
							// TODO Auto-generated catch block
							try {
								timestamp = dateFormat2.parse(
										(String) listTuples.get(0)).getTime();
							} catch (ParseException e2) {
								// TODO Auto-generated catch block
								try {
									timestamp = dateFormat3.parse(
											(String) listTuples.get(0))
											.getTime();
								} catch (ParseException e3) {
									// TODO Auto-generated catch block
									e3.printStackTrace();
								}
							}
						}
					}
					point = new DataPointTimestamp((double) timestamp,
							(double) listTuples.get(1));
					dataPoints.add(point);
				}
			}
		}
		return dataPoints;
	}

	/**
	 * Fetches measurementTimestamp, use case id, cpu usage and battery power
	 * from TSDB and puts them into objects
	 * 
	 * @return
	 */
	public List<DataPointWithTimestampAndThreeMeasurements> getDataPointsWithFourMeasurements() {
		List<DataPointWithTimestampAndThreeMeasurements> dataPoints = new LinkedList<DataPointWithTimestampAndThreeMeasurements>();
		String command = "select time, use_case_id, cpu_usage, battery_power from %s where time >= '2017-01-01T00:00:00Z' "
				+ "AND time <= now() fill(none)";
		command = String.format(command, TABLE_MOBILE_DATA);

		Query query = new Query(command, DATABASE_NAME);

		QueryResult qResult = influxDB.query(query);

		DataPointWithTimestampAndThreeMeasurements point;

		for (Result result : qResult.getResults()) {
			for (Series serie : result.getSeries()) {
				for (List<Object> listTuples : serie.getValues()) {
					DateFormat dateFormat = new SimpleDateFormat(
							"yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
					DateFormat dateFormat1 = new SimpleDateFormat(
							"yyyy-MM-dd'T'HH:mm:ss.SSXXX");
					DateFormat dateFormat2 = new SimpleDateFormat(
							"yyyy-MM-dd'T'HH:mm:ss.SXXX");
					DateFormat dateFormat3 = new SimpleDateFormat(
							"yyyy-MM-dd'T'HH:mm:SSSXXX");
					long timestamp = 0;
					try {
						timestamp = dateFormat
								.parse((String) listTuples.get(0)).getTime();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						try {
							timestamp = dateFormat1.parse(
									(String) listTuples.get(0)).getTime();
						} catch (ParseException e1) {
							// TODO Auto-generated catch block
							try {
								timestamp = dateFormat2.parse(
										(String) listTuples.get(0)).getTime();
							} catch (ParseException e2) {
								// TODO Auto-generated catch block
								try {
									timestamp = dateFormat3.parse(
											(String) listTuples.get(0))
											.getTime();
								} catch (ParseException e3) {
									// TODO Auto-generated catch block
									e3.printStackTrace();
								}
							}
						}
					}
					point = new DataPointWithTimestampAndThreeMeasurements(
							timestamp, (String) listTuples.get(1),
							(double) listTuples.get(2),
							(double) listTuples.get(3),
							(double) listTuples.get(4));
					dataPoints.add(point);
				}
			}
		}
		return dataPoints;
	}

	/**
	 * Fetches measurementTimestamp, use case id, cpu usage and battery power
	 * from TSDB and puts them into objects
	 * 
	 * @return
	 */
	public List<DataPointWithTimestampAndSixMeasurements> getDataPointsWithSevenMeasurements() {
		List<DataPointWithTimestampAndSixMeasurements> dataPoints = new LinkedList<DataPointWithTimestampAndSixMeasurements>();
		String command = "select time, use_case_id, cpu_usage, battery_power, use_case_duration, avg_cpu_usage, battery_power_difference from %s where time >= '2017-01-01T00:00:00Z' "
				+ "AND time <= now() fill(none)";
		command = String.format(command, TABLE_MOBILE_DATA);

		Query query = new Query(command, DATABASE_NAME);

		QueryResult qResult = influxDB.query(query);

		DataPointWithTimestampAndSixMeasurements point;

		for (Result result : qResult.getResults()) {
			for (Series serie : result.getSeries()) {
				for (List<Object> listTuples : serie.getValues()) {
					DateFormat dateFormat = new SimpleDateFormat(
							"yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
					DateFormat dateFormat1 = new SimpleDateFormat(
							"yyyy-MM-dd'T'HH:mm:ss.SSXXX");
					DateFormat dateFormat2 = new SimpleDateFormat(
							"yyyy-MM-dd'T'HH:mm:ss.SXXX");
					DateFormat dateFormat3 = new SimpleDateFormat(
							"yyyy-MM-dd'T'HH:mm:SSSXXX");
					long timestamp = 0;
					try {
						timestamp = dateFormat
								.parse((String) listTuples.get(0)).getTime();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						try {
							timestamp = dateFormat1.parse(
									(String) listTuples.get(0)).getTime();
						} catch (ParseException e1) {
							// TODO Auto-generated catch block
							try {
								timestamp = dateFormat2.parse(
										(String) listTuples.get(0)).getTime();
							} catch (ParseException e2) {
								// TODO Auto-generated catch block
								try {
									timestamp = dateFormat3.parse(
											(String) listTuples.get(0))
											.getTime();
								} catch (ParseException e3) {
									// TODO Auto-generated catch block
									e3.printStackTrace();
								}
							}
						}
					}
					point = new DataPointWithTimestampAndSixMeasurements(
							timestamp, (String) listTuples.get(1),
							(double) listTuples.get(2),
							(double) listTuples.get(3),
							(double) listTuples.get(4),
							(double) listTuples.get(5),
							(double) listTuples.get(6));
					dataPoints.add(point);
				}
			}
		}
		return dataPoints;
	}

	/**
	 * data point that holds timestamp and a particular measurement
	 * 
	 * @author Alper Hi
	 *
	 */
	public class DataPointTimestamp {
		public double getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(double timestamp) {
			this.timestamp = timestamp;
		}

		public double getMeasurement() {
			return measurement;
		}

		public void setMeasurement(double value) {
			this.measurement = value;
		}

		double timestamp;
		double measurement;

		DataPointTimestamp(double timestamp, double value) {
			this.timestamp = timestamp;
			this.measurement = value;
		}
	}

	/**
	 * data point that holds timestamp and four particular measurements
	 * 
	 * @author Alper Hi
	 *
	 */
	public class DataPointWithTimestampAndThreeMeasurements {

		public String getTraceId() {
			return useCaseId;
		}

		public void setTraceId(String useCaseId) {
			this.useCaseId = useCaseId;
		}

		public double getMeasurement1() {
			return measurement1;
		}

		public void setMeasurement1(double measurement1) {
			this.measurement1 = measurement1;
		}

		public double getMeasurement2() {
			return measurement2;
		}

		public void setMeasurement2(double measurement2) {
			this.measurement2 = measurement2;
		}

		public double getMeasurement3() {
			return measurement3;
		}

		public void setMeasurement3(double measurement3) {
			this.measurement3 = measurement3;
		}

		DataPointWithTimestampAndThreeMeasurements(double timestamp,
				String useCaseId, double measurement1, double measurement2,
				double measurement3) {
			this.useCaseId = useCaseId;
			this.measurement1 = measurement1;
			this.measurement2 = measurement2;
			this.measurement3 = measurement3;
		}

		private String useCaseId = null;
		private double measurement1 = 0;
		private double measurement2 = 0;
		private double measurement3 = 0;

	}

	/**
	 * data point that holds timestamp and four particular measurements
	 * 
	 * @author Alper Hi
	 *
	 */
	public class DataPointWithTimestampAndSixMeasurements {

		public String getTraceId() {
			return useCaseId;
		}

		public void setTraceId(String useCaseId) {
			this.useCaseId = useCaseId;
		}

		public double getMeasurement1() {
			return measurement1;
		}

		public void setMeasurement1(double measurement1) {
			this.measurement1 = measurement1;
		}

		public double getMeasurement2() {
			return measurement2;
		}

		public void setMeasurement2(double measurement2) {
			this.measurement2 = measurement2;
		}

		public double getMeasurement3() {
			return measurement3;
		}

		public void setMeasurement3(double measurement3) {
			this.measurement3 = measurement3;
		}

		public double getMeasurement4() {
			return measurement4;
		}

		public void setMeasurement4(double measurement4) {
			this.measurement4 = measurement4;
		}

		public double getMeasurement5() {
			return measurement5;
		}

		public void setMeasurement5(double measurement5) {
			this.measurement5 = measurement5;
		}

		public double getMeasurement6() {
			return measurement6;
		}

		public void setMeasurement6(double measurement6) {
			this.measurement6 = measurement6;
		}

		DataPointWithTimestampAndSixMeasurements(double timestamp,
				String useCaseId, double measurement1, double measurement2,
				double measurement3, double measurement4, double measurement5) {
			this.useCaseId = useCaseId;
			this.measurement1 = measurement1;
			this.measurement2 = measurement2;
			this.measurement3 = measurement3;
			this.measurement4 = measurement4;
			this.measurement5 = measurement5;
		}

		private String useCaseId = null;
		private double measurement1 = 0;
		private double measurement2 = 0;
		private double measurement3 = 0;
		private double measurement4 = 0;
		private double measurement5 = 0;
		private double measurement6 = 0;

	}

	/**
	 * thread for automatically writing data points into the mobile data
	 * database table
	 * 
	 * @author Alper Hi
	 *
	 */
	// private static class TimeseriesThread extends Thread {
	//
	// /**
	// * In milliseconds
	// */
	// private static final int INTERVAL = 1000;
	//
	// @Override
	// public void run() {
	// InfluxDBConnectorMobile test = new InfluxDBConnectorMobile();
	// test.connect();
	//
	// double responseTime = 0;
	// double gcTime = 0;
	// double cpuTime = 5000;
	// long traceId = 1;
	//
	// while (traceId <= 100) {
	//
	// if (traceId == 25 || traceId == 50 || traceId == 75) {
	// gcTime = ThreadLocalRandom.current().nextInt(25000, 50000);
	// responseTime = ThreadLocalRandom.current().nextInt(50000,
	// 75000);
	// } else {
	// gcTime = ThreadLocalRandom.current().nextInt(500, 3000);
	// responseTime = ThreadLocalRandom.current().nextInt(3000,
	// 5000);
	// }
	// test.writeData(responseTime, cpuTime, gcTime, traceId,
	// System.currentTimeMillis());
	// traceId++;
	// try {
	// Thread.sleep(INTERVAL);
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// }
}
