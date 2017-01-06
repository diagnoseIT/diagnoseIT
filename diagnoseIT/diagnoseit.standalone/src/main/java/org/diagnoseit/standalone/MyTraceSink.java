package org.diagnoseit.standalone;

import java.util.LinkedList;
import java.util.List;

import org.diagnoseit.spike.shared.TraceSink;
import org.spec.research.open.xtrace.api.core.Trace;

class MyTraceSink implements TraceSink {
	private List<Trace> listTrace = new LinkedList<Trace>();

	public void appendTrace(Trace trace) {
		listTrace.add(trace);
	}

	public List<Trace> getAllTraces() {
		return listTrace;
	}
}
