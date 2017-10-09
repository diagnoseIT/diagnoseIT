package org.diagnoseit.standalone;

import java.util.Collections;
import java.util.List;

import org.diagnoseit.engine.session.ISessionCallback;
import org.diagnoseit.rules.result.ProblemOccurrence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spec.research.open.xtrace.api.core.Trace;

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

	public static void main(String[] args) throws ClassNotFoundException {
		startLauncher(new TraceCreatorForTesting().createTrace());
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
