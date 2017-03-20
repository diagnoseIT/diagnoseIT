package org.diagnoseit.standalone;

import java.util.Collections;
import java.util.List;

import org.diagnoseit.engine.session.ISessionCallback;
import org.diagnoseit.rules.result.ProblemOccurrence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spec.research.open.xtrace.adapters.introscope.source.IntroscopeTraceConverter;
import org.spec.research.open.xtrace.api.core.Trace;
import org.spec.research.open.xtrace.shared.TraceConverter;

/**
 * Launcher for rules that analyze a single trace.
 * 
 * @author Alper Hi
 *
 */
public class Launcher {

	/**
	 * Rules that should be executed.
	 */
	public enum RulePackage {
		DefaultPackage("org.diagnoseit.rules.impl"),
		MobilePackage("org.diagnoseit.rules.mobile.impl");

		private String packageName;

		RulePackage(String packageName) {
			this.packageName = packageName;
		}

		public String getPackageName() {
			return this.packageName;
		}

	};

	/**
	 * Path to traces that should be analyzed.
	 */
	private static final String INTROSCOPE_FILE = "C:/Users/Alper Hi/Desktop/Universit�t/Bachelorarbeit/Traces_CA/CA_Trace1.xml";

	private static final String DYNATRACE_FILE = "path to dynatrace trace file";

	private static final String INSPECTIT_FILE = "";

	private static final String KIEKER_FILE = "path to kieker file";

	public static void main(String[] args) throws ClassNotFoundException {
		
		Trace trace = TraceCreator.getTestTrace(true, 100, 3);
		System.out.println(trace.getRoot());
		startLauncher(trace);
		
		// IntroscopeTraceConverter itc = new IntroscopeTraceConverter();
		// List<Trace> traces = itc.convertTraces(INTROSCOPE_FILE);
		// System.out.println(traces.get(0));
		// startLauncher(traces.get(0));
	}

	/**
	 * @param trace
	 * @throws ClassNotFoundException
	 */
	public static void startLauncher(Trace trace) throws ClassNotFoundException {
		startLauncher(trace, RulePackage.MobilePackage);
	}

	/**
	 * @param trace
	 * @throws ClassNotFoundException
	 */
	public static void startLauncher(Trace trace, RulePackage rulePackage)
			throws ClassNotFoundException {
		DiagnoseIT diagnoseIT = new DiagnoseIT(
				Collections.singletonList(rulePackage.getPackageName()));
		diagnoseIT.init(new ResultHandler());

		long baseline = 1000L;

		diagnoseIT.diagnose(trace, baseline);
	}

	private static class ResultHandler implements
			ISessionCallback<List<ProblemOccurrence>> {
		/** The logger of this class. */
		private static final Logger log = LoggerFactory
				.getLogger(Launcher.class);

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onSuccess(List<ProblemOccurrence> result) {
			System.out.println("Success!!");
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

}
