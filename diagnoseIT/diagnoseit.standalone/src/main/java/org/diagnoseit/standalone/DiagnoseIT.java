package org.diagnoseit.standalone;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.diagnoseit.engine.DiagnosisEngine;
import org.diagnoseit.engine.DiagnosisEngineConfiguration;
import org.diagnoseit.engine.IDiagnosisEngine;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.session.ISessionCallback;
import org.diagnoseit.engine.session.SessionVariables;
import org.diagnoseit.rules.RuleConstants;
import org.diagnoseit.rules.result.ProblemInstanceResultCollector;
import org.diagnoseit.rules.result.ProblemOccurrence;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spec.research.open.xtrace.api.core.Trace;


public class DiagnoseIT implements Runnable{
	private static final long TIMEOUT = 50;

	/** The logger of this class. */
	private static final Logger log = LoggerFactory.getLogger(DiagnoseIT.class);
	
	
	private final int capacity = 100;

	// influx
	private final BlockingQueue<DiagnosisInput> queue = new LinkedBlockingQueue<>(capacity);

	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	private final List<String> rulesPackages;
	
	private IDiagnosisEngine<Trace> engine;
	
	public DiagnoseIT(List<String> rulesPackages) {
		this.rulesPackages = rulesPackages;
	}


	// influx 
	public boolean diagnose(Trace trace, long baseline) {
		try {
			// influx
			return queue.offer(new DiagnosisInput(trace, baseline), TIMEOUT, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			return false;
		}
	}

	public int diagnose(Collection<Pair<Trace, Long>> traceBaselinePairs) {
		int count = 0;
		for (Pair<Trace, Long> invocationBaselinePair : traceBaselinePairs) {
			boolean successfullySubmitted = diagnose(invocationBaselinePair.getLeft(), invocationBaselinePair.getRight());
			if (!successfullySubmitted) {
				break;
			}
			count++;
		}
		return count;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		try {
			while (true) {
				DiagnosisInput diagnosisInput = queue.take();
				SessionVariables sessionVariables = new SessionVariables();
				sessionVariables.put(RuleConstants.VAR_BASELINE, diagnosisInput.getBaseline());
				engine.analyze(diagnosisInput.getTrace(), sessionVariables);
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public void init(ISessionCallback<List<ProblemOccurrence>> resultHandler) throws ClassNotFoundException {
		
		Set<Class<?>> ruleClasses = new HashSet<>();
		for (String packageName : rulesPackages) {
			Reflections reflections = new Reflections(packageName);
			Set<Class<?>> subTypesOf = reflections.getTypesAnnotatedWith(Rule.class);
			ruleClasses.addAll(subTypesOf);
		}

		DiagnosisEngineConfiguration<Trace, List<ProblemOccurrence>> configuration = new DiagnosisEngineConfiguration<Trace, List<ProblemOccurrence>>();

		configuration.setNumSessionWorkers(2);
		configuration.setRuleClasses(ruleClasses);
		configuration.setResultCollector(new ProblemInstanceResultCollector<Trace>());
		configuration.setSessionCallback(resultHandler);

		engine = new DiagnosisEngine<>(configuration);
		executor.execute(this);
		if (log.isInfoEnabled()) {
			log.info("|-Diagnosis Service active...");
		}
	}

	private static class DiagnosisInput {
		private final Trace trace;
		private final double baseline;

		/**
		 * @param invocation
		 * @param baseline
		 */
		public DiagnosisInput(Trace trace, double baseline) {
			this.trace = trace;
			this.baseline = baseline;
		}

		/**
		 * Gets {@link #trace}.
		 *
		 * @return {@link #trace}
		 */
		public Trace getTrace() {
			return trace;
		}

		/**
		 * Gets {@link #baseline}.
		 *
		 * @return {@link #baseline}
		 */
		public double getBaseline() {
			return baseline;
		}
	}
	

}
