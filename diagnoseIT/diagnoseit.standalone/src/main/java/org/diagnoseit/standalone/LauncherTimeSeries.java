package org.diagnoseit.standalone;

import java.util.Collections;

import org.diagnoseit.engine.session.DefaultSessionResult;
import org.diagnoseit.engine.session.ISessionCallback;
import org.diagnoseit.rules.mobile.timeseries.impl.InfluxDBConnectorMobile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Launcher for time series rules. Periodically triggered.
 * 
 * @author Alper Hi
 *
 */
public class LauncherTimeSeries {

	private static final String RULES_PACKAGE = "org.diagnoseit.rules.mobile.timeseries.impl";

	public static void main(String[] args) throws ClassNotFoundException {
		Thread timeThread = new TimeseriesThread();
		timeThread.start();
	}

	private static class ResultHandler implements
			ISessionCallback<DefaultSessionResult<InfluxDBConnectorMobile>> {
		/** The logger of this class. */
		private static final Logger log = LoggerFactory
				.getLogger(Launcher.class);

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onSuccess(DefaultSessionResult<InfluxDBConnectorMobile> result) {
			// TODO: Do Something with diagnosis result
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onFailure(Throwable t) {
			log.warn("Failed conducting diagnosis!", t);
		}
	}

	private static class TimeseriesThread extends Thread {

		/**
		 * In milliseconds
		 */
		private static final int INTERVAL = 100000;

		@Override
		public void run() {

			while (true) {

				DiagnoseITTimeseries diagnoseIT = new DiagnoseITTimeseries(
						Collections.singletonList(RULES_PACKAGE));
				try {
					diagnoseIT.init(new ResultHandler());
					diagnoseIT.diagnose(new InfluxDBConnectorMobile());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				try {
					Thread.sleep(INTERVAL);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

}
