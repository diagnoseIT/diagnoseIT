package org.diagnoseit.rules.timeseries.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
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
public class InfluxDBConnector {

	/**
	 * trace Database is used for diagnoseIT time series analysis
	 */
	private static final String DATABASE_NAME = "trace";
	private static final String DATABASE_URL = "http://127.0.0.1:8086";
	private static final String DATABASE_USERNAME = "Alper";
	private static final String DATABASE_PASSWORD = "Alper";

	private static final String INFLUXDB_TAG_TRACE = "TraceID";

	/**
	 * timing information database table with trace data, also saves traceIDs
	 */
	private static final String TABLE_TIMING_INFORMATION = "TimingInformation";
	private static final String COLUMN_RESPONSE_TIME = "response_time";
	private static final String COLUMN_GC_TIME = "garbage_collector_time";
	private static final String COLUMN_CPU_TIME = "cpu_time";

	private static final String RETENTION_POLICY = "autogen";

	private InfluxDB influxDB = null;

	/**
	 * main method for testing
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		Thread timeThread = new TimeseriesThread();
		timeThread.start();
		InfluxDBConnector test = new InfluxDBConnector();
		test.connect();

		test.writeData(242, 5, 3, 5, System.currentTimeMillis());
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

		Query query = new Query("SELECT * FROM " + TABLE_TIMING_INFORMATION,
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
	 * Fetches timestamp and response time from timing information database
	 * table and puts them into objects
	 * 
	 * @return
	 */
	public List<DataPointTimestamp> getDataPointsWithTimestamp() {
		List<DataPointTimestamp> dataPoints = new LinkedList<DataPointTimestamp>();
		String command = "select time, response_time from %s where time >= '2016-01-01T00:00:00Z' "
				+ "AND time <= now() fill(none)";
		command = String.format(command, TABLE_TIMING_INFORMATION);

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
	 * fetches response times, gc times and corresponding Trace IDs from
	 * database
	 * 
	 * @return
	 */
	public List<DataPointTraceID> getDataPointsWithTraceID() {
		List<DataPointTraceID> dataPoints = new LinkedList<DataPointTraceID>();
		String command = "select TraceID, response_time, garbage_collector_time from %s where time >= '2016-01-01T00:00:00Z' "
				+ "AND time <= now() fill(none)";
		command = String.format(command, TABLE_TIMING_INFORMATION);

		Query query = new Query(command, DATABASE_NAME);

		QueryResult qResult = influxDB.query(query);

		DataPointTraceID point;

		for (Result result : qResult.getResults()) {
			for (Series serie : result.getSeries()) {
				for (List<Object> listTuples : serie.getValues()) {
					point = new DataPointTraceID((String) listTuples.get(1),
							(double) listTuples.get(2),
							(double) listTuples.get(3));
					dataPoints.add(point);
				}
			}
		}
		return dataPoints;
	}

	/**
	 * fetches response times and corresponding cpu times from database
	 * 
	 * @return
	 */
	public List<DataPointTwoMeasurements> getDataPointsResponseAndCPUTimes() {
		List<DataPointTwoMeasurements> dataPoints = new LinkedList<DataPointTwoMeasurements>();
		String command = "select response_time, cpu_time from %s where time >= '2016-01-01T00:00:00Z' "
				+ "AND time <= now() fill(none)";
		command = String.format(command, TABLE_TIMING_INFORMATION);

		Query query = new Query(command, DATABASE_NAME);

		QueryResult qResult = influxDB.query(query);

		DataPointTwoMeasurements point;

		for (Result result : qResult.getResults()) {
			for (Series serie : result.getSeries()) {
				for (List<Object> listTuples : serie.getValues()) {
					point = new DataPointTwoMeasurements(
							(double) listTuples.get(1),
							(double) listTuples.get(2));
					dataPoints.add(point);
				}
			}
		}
		return dataPoints;
	}

	/**
	 * Fetches TraceIDs from timing information database table
	 * 
	 * @return
	 */
	public List<String> getTraceIds() {
		List<String> listTraceIds = new LinkedList<String>();

		Query query = new Query("SHOW TAG VALUES FROM "
				+ TABLE_TIMING_INFORMATION + " WITH KEY = TraceID",
				DATABASE_NAME);
		QueryResult qResult = influxDB.query(query);

		for (Result result : qResult.getResults()) {
			for (Series serie : result.getSeries()) {
				for (List<Object> listTuples : serie.getValues()) {
					listTraceIds.add((String) listTuples.get(1));
				}
			}
		}
		return listTraceIds;
	}

	/**
	 * Only for testing
	 * 
	 * @return
	 */
	private List<Double> getTestValues() {

		List<Double> aggregatedResponseTimes = new LinkedList<Double>();
		// aggregatedResponseTimes.add(0.);
		// aggregatedResponseTimes.add(3.);
		// aggregatedResponseTimes.add(5.);
		// aggregatedResponseTimes.add(0.);
		// aggregatedResponseTimes.add(3.);
		// aggregatedResponseTimes.add(5.);
		// aggregatedResponseTimes.add(0.);

		Random random = new Random();
		for (int i = 0; i < 10; i++) {
			aggregatedResponseTimes.add(1.0 * random.nextInt(10));
		}

		String values = "";
		for (Double double1 : aggregatedResponseTimes) {
			values += double1 + ", ";
		}

		return aggregatedResponseTimes;
	}

	/**
	 * Only for testing
	 * 
	 * @return
	 */
	private List<Double> getGCTestValues() {

		List<Double> aggregatedGCTimes = new LinkedList<Double>();
		// aggregatedResponseTimes.add(0.);
		// aggregatedResponseTimes.add(3.);
		// aggregatedResponseTimes.add(5.);
		// aggregatedResponseTimes.add(0.);
		// aggregatedResponseTimes.add(3.);
		// aggregatedResponseTimes.add(5.);
		// aggregatedResponseTimes.add(0.);

		Random random = new Random();
		for (int i = 0; i < 10; i++) {
			aggregatedGCTimes.add(1.0 * random.nextInt(10));
		}

		String values = "";
		for (Double double1 : aggregatedGCTimes) {
			values += double1 + ", ";
		}

		return aggregatedGCTimes;
	}

	/**
	 * Fetches response times from timing information database table and puts
	 * them into a list
	 * 
	 * @return
	 */
	public List<Double> readResponseTimes() {

		List<Double> listResponseTime = new LinkedList<Double>();
		String command = "select response_time from %s where time >= '2016-01-01T00:00:00Z' "
				+ "AND time <= now() fill(none)";
		command = String.format(command, TABLE_TIMING_INFORMATION);

		Query query = new Query(command, DATABASE_NAME);

		QueryResult qResult = influxDB.query(query);

		for (Result result : qResult.getResults()) {
			for (Series serie : result.getSeries()) {
				for (List<Object> listTuples : serie.getValues()) {
					listResponseTime.add((Double) (listTuples.get(1)));
				}
			}
		}
		return listResponseTime;
	}

	/**
	 * Fetches aggregated response times from timing information database table
	 * and puts them into a list
	 * 
	 * @return
	 * @param interval
	 *            specifies the interval within the response times should be
	 *            aggregated
	 */
	public List<Double> readAggregatedResponseTimes(final String interval) {

		List<Double> listResponseTime = new LinkedList<Double>();
		String command = "select mean(response_time) from %s where time >= '2016-01-01T00:00:00Z' "
				+ "AND time <= now() group by time(%s) fill(none)";
		command = String.format(command, TABLE_TIMING_INFORMATION, interval);

		Query query = new Query(command, DATABASE_NAME);

		QueryResult qResult = influxDB.query(query);

		for (Result result : qResult.getResults()) {
			for (Series serie : result.getSeries()) {
				for (List<Object> listTuples : serie.getValues()) {
					listResponseTime.add((Double) (listTuples.get(1)));
				}
			}
		}

		return listResponseTime;
		// return getTestValues();
	}

	/**
	 * Fetches response times from timing information database table and sums
	 * them up
	 * 
	 * @return
	 */
	public double readSumResponseTimes() {

		double sumResponseTime = 0;
		String command = "select sum(response_time) from %s where time >= '2016-01-01T00:00:00Z' "
				+ "AND time <= now() fill(none)";
		command = String.format(command, TABLE_TIMING_INFORMATION);

		Query query = new Query(command, DATABASE_NAME);

		QueryResult qResult = influxDB.query(query);

		for (Result result : qResult.getResults()) {
			for (Series serie : result.getSeries()) {
				for (List<Object> listTuples : serie.getValues()) {
					sumResponseTime = (double) listTuples.get(1);
				}
			}
		}

		return sumResponseTime;
	}

	/**
	 * Fetches average response time from timing information database table
	 * 
	 * @return
	 */
	public double readAVGResponseTime() {

		double avgResponseTime = 0;
		String command = "select mean(response_time) from %s where time >= '2016-01-01T00:00:00Z' "
				+ "AND time <= now() fill(none)";
		command = String.format(command, TABLE_TIMING_INFORMATION);

		Query query = new Query(command, DATABASE_NAME);

		QueryResult qResult = influxDB.query(query);

		for (Result result : qResult.getResults()) {
			for (Series serie : result.getSeries()) {
				for (List<Object> listTuples : serie.getValues()) {
					avgResponseTime = (double) listTuples.get(1);
				}
			}
		}

		return avgResponseTime;
	}

	/**
	 * Fetches aggregated gc times from timing information database table and
	 * puts them into a list
	 * 
	 * @return
	 * @param interval
	 *            specifies the interval within the gc times should be
	 *            aggregated
	 */
	public List<Double> readAggregatedGCTimes(final String interval) {

		List<Double> listGCTime = new LinkedList<Double>();
		String command = "select mean(garbage_collector_time) from %s where time >= '2016-01-01T00:00:00Z' "
				+ "AND time <= now() group by time(%s) fill(none)";
		command = String.format(command, TABLE_TIMING_INFORMATION, interval);

		Query query = new Query(command, DATABASE_NAME);

		QueryResult qResult = influxDB.query(query);

		for (Result result : qResult.getResults()) {
			for (Series serie : result.getSeries()) {
				for (List<Object> listTuples : serie.getValues()) {
					listGCTime.add((Double) (listTuples.get(1)));
				}
			}
		}

		return listGCTime;
	}

	/**
	 * Fetches cpu times from timing information database table and puts them
	 * into a list
	 * 
	 * @return
	 */
	public List<Double> readCPUTimes() {

		List<Double> listCPUTime = new LinkedList<Double>();
		String command = "select cpu_time from %s where time >= '2016-01-01T00:00:00Z' "
				+ "AND time <= now() fill(none)";
		command = String.format(command, TABLE_TIMING_INFORMATION);

		Query query = new Query(command, DATABASE_NAME);

		QueryResult qResult = influxDB.query(query);

		for (Result result : qResult.getResults()) {
			for (Series serie : result.getSeries()) {
				for (List<Object> listTuples : serie.getValues()) {
					listCPUTime.add((Double) (listTuples.get(1)));
				}
			}
		}
		return listCPUTime;
	}

	/**
	 * Fetches aggregated cpu times from timing information database table and
	 * puts them into a list
	 * 
	 * @return
	 * @param interval
	 *            specifies the interval within the cpu times should be
	 *            aggregated
	 */
	public List<Double> readAggregatedCPUTimes(final String interval) {

		List<Double> listGCTime = new LinkedList<Double>();
		String command = "select mean(cpu_time) from %s where time >= '2016-01-01T00:00:00Z' "
				+ "AND time <= now() group by time(%s) fill(none)";
		command = String.format(command, TABLE_TIMING_INFORMATION, interval);

		Query query = new Query(command, DATABASE_NAME);

		QueryResult qResult = influxDB.query(query);

		for (Result result : qResult.getResults()) {
			for (Series serie : result.getSeries()) {
				for (List<Object> listTuples : serie.getValues()) {
					listGCTime.add((Double) (listTuples.get(1)));
				}
			}
		}

		return listGCTime;

	}

	/**
	 * Fetches cpu times from timing information database table and sums them up
	 * 
	 * @return
	 */
	public double readSumCPUTimes() {

		double sumCPUTime = 0;
		String command = "select sum(cpu_time) from %s where time >= '2016-01-01T00:00:00Z' "
				+ "AND time <= now() fill(none)";
		command = String.format(command, TABLE_TIMING_INFORMATION);

		Query query = new Query(command, DATABASE_NAME);

		QueryResult qResult = influxDB.query(query);

		for (Result result : qResult.getResults()) {
			for (Series serie : result.getSeries()) {
				for (List<Object> listTuples : serie.getValues()) {
					sumCPUTime = (double) listTuples.get(1);
				}
			}
		}

		return sumCPUTime;
	}

	/**
	 * Fetches average response time from timing information database table
	 * 
	 * @return
	 */
	public double readAVGCPUTimes() {

		double avgCPUTime = 0;
		String command = "select mean(cpu_time) from %s where time >= '2016-01-01T00:00:00Z' "
				+ "AND time <= now() fill(none)";
		command = String.format(command, TABLE_TIMING_INFORMATION);

		Query query = new Query(command, DATABASE_NAME);

		QueryResult qResult = influxDB.query(query);

		for (Result result : qResult.getResults()) {
			for (Series serie : result.getSeries()) {
				for (List<Object> listTuples : serie.getValues()) {
					avgCPUTime = (double) listTuples.get(1);
				}
			}
		}

		return avgCPUTime;
	}

	/**
	 * writes a data point to the timing information database table
	 * 
	 * @param responseTime
	 * @param cpuTime
	 * @param gcTime
	 * @param traceId
	 * @param traceTimestamp
	 */
	public void writeData(double responseTime, double cpuTime, double gcTime,
			long traceId, long traceTimestamp) {

		Point point1 = Point.measurement(TABLE_TIMING_INFORMATION)
				.tag(INFLUXDB_TAG_TRACE, String.valueOf(traceId))
				.time(traceTimestamp, TimeUnit.MILLISECONDS)
				.addField(COLUMN_RESPONSE_TIME, responseTime)
				.addField(COLUMN_CPU_TIME, cpuTime)
				.addField(COLUMN_GC_TIME, gcTime).build();

		influxDB.write(DATABASE_NAME, RETENTION_POLICY, point1);
	}

	/**
	 * thread for automatically writing data points into the timing information
	 * database table
	 * 
	 * @author Alper Hi
	 *
	 */
	private static class TimeseriesThread extends Thread {

		/**
		 * In milliseconds
		 */
		private static final int INTERVAL = 1000;

		@Override
		public void run() {
			InfluxDBConnector test = new InfluxDBConnector();
			test.connect();

			double responseTime = 0;
			double gcTime = 0;
			double cpuTime = 5000;
			long traceId = 1;

			while (traceId <= 100) {

				if (traceId == 25 || traceId == 50 || traceId == 75) {
					gcTime = ThreadLocalRandom.current().nextInt(25000, 50000);
					responseTime = ThreadLocalRandom.current().nextInt(50000,
							75000);
				} else {
					gcTime = ThreadLocalRandom.current().nextInt(500, 3000);
					responseTime = ThreadLocalRandom.current().nextInt(3000,
							5000);
				}
				test.writeData(responseTime, cpuTime, gcTime, traceId,
						System.currentTimeMillis());
				traceId++;
				try {
					Thread.sleep(INTERVAL);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * data point that holds Trace ID and two measurements
	 * 
	 * @author Alper Hi
	 *
	 */
	public class DataPointTraceID {
		public String getTraceId() {
			return traceId;
		}

		public void setTraceId(String traceId) {
			this.traceId = traceId;
		}

		public double getMeasurement1() {
			return measurement1;
		}

		public void setMeasurement1(double measurement1) {
			this.measurement1 = measurement1;
		}

		DataPointTraceID(String traceId, double measurement1,
				double measurement2) {
			this.traceId = traceId;
			this.measurement1 = measurement1;
			this.measurement2 = measurement2;
		}

		public double getMeasurement2() {
			return measurement2;
		}

		public void setMeasurement2(double measurement2) {
			this.measurement2 = measurement2;
		}

		private String traceId = null;
		private double measurement1 = 0;
		private double measurement2 = 0;

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
	 * data point that holds two particular measurements
	 * 
	 * @author Alper Hi
	 *
	 */
	public class DataPointTwoMeasurements {
		public double getMeasurementOne() {
			return measurement1;
		}

		public void setMeasurementOne(double measurement1) {
			this.measurement1 = measurement1;
		}

		public double getMeasurementTwo() {
			return measurement2;
		}

		public void setMeasurementTwo(double measurement2) {
			this.measurement2 = measurement2;
		}

		double measurement1;
		double measurement2;

		DataPointTwoMeasurements(double measurement2, double measurement1) {
			this.measurement2 = measurement2;
			this.measurement1 = measurement1;
		}
	}
}
