package org.diagnoseit.standalone;

import java.util.Collections;
import java.util.List;

import org.diagnoseit.engine.session.ISessionCallback;
import org.diagnoseit.rules.result.ProblemOccurrence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.cta.api.core.Trace;

public class Launcher {

	
	private static final String RULES_PACKAGE = "org.diagnoseit.rules.impl";

	public static void main(String[] args) throws ClassNotFoundException {
		DiagnoseIT diagnoseIT = new DiagnoseIT(
				Collections.singletonList(RULES_PACKAGE));
		diagnoseIT.init(new ResultHandler());
		
		// TODO: data sink -> diagnoseIT
		Trace trace = null;
		long baseline = 1000L;
		diagnoseIT.diagnose(trace, baseline);
	}
	
	private static class ResultHandler implements ISessionCallback<List<ProblemOccurrence>> {
		/** The logger of this class. */
		private static final Logger log = LoggerFactory.getLogger(Launcher.class);
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onSuccess(List<ProblemOccurrence> result) {
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
