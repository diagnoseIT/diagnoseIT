package org.diagnoseit.traceservices.result;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class InteractiveRequestSink implements ProblemInstanceSink {

	private int dataSet = 0;
	private final Map<ProblemInstanceID, ProblemInstance> problemInstances;

	public InteractiveRequestSink() {
		problemInstances = new HashMap<ProblemInstanceID, ProblemInstance>();
	}

	@Override
	public void putProblemInstance(ProblemInstance problemInstance) {
		ProblemInstance pInstance = problemInstances.get(problemInstance.getInstanceID());
		if (pInstance == null) {
			problemInstances.put(problemInstance.getInstanceID(), problemInstance);
		} else {
			pInstance.update(problemInstance);
		}
	}

	@Override
	public void putProblemInstances(Collection<ProblemInstance> pInstances) {
		for (ProblemInstance pInstance : pInstances) {
			putProblemInstance(pInstance);
		}

	}

	@Override
	public synchronized void dataSetNotification() {
		dataSet++;
		this.notifyAll();
	}

	public synchronized void awaitDataSet(int num) {
		while (dataSet < num) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * @return the problemInstances
	 */
	public Collection<ProblemInstance> getProblemInstances() {
		return problemInstances.values();
	}

}
