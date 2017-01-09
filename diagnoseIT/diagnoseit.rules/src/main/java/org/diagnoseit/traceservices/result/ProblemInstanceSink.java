package org.diagnoseit.traceservices.result;

import java.util.Collection;


public interface ProblemInstanceSink {

	void putProblemInstance(ProblemInstance pInstance);
	void putProblemInstances(Collection<ProblemInstance> pInstances);
	void dataSetNotification();

}
