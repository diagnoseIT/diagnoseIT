package org.diagnoseit.traceservices.result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProblemInstaceRegistry implements ProblemInstanceSink{
	private static ProblemInstaceRegistry instance;

	public static ProblemInstaceRegistry getInstance() {
		if (instance == null) {
			instance = new ProblemInstaceRegistry();
		}
		return instance;
	}

	private final Map<ProblemInstanceID, ProblemInstance> problemInstances;

	private ProblemInstaceRegistry() {
		problemInstances = new HashMap<ProblemInstanceID, ProblemInstance>();
	}

	/**
	 * @return the problemInstances
	 */
	public List<ProblemInstance> getProblemInstances() {
		return Collections.unmodifiableList(new ArrayList<ProblemInstance>(problemInstances.values()));
	}

	@Override
	public String toString() {
		String result = "###############################################################################################\n";
		int counter = 1;
		for (ProblemInstance pInstance : problemInstances.values()) {
			result += "\n\nProblem instance " + counter + "\n" + pInstance.toString();
			counter++;
		}
		result += "###############################################################################################\n";
		return result;
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
		for(ProblemInstance pInstance : pInstances){
			putProblemInstance(pInstance);
		}
		
	}

	@Override
	public void dataSetNotification() {
	// nothing to do here
		
	}

}
