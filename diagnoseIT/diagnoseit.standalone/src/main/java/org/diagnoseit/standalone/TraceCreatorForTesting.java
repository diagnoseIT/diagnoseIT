package org.diagnoseit.standalone;

import java.util.Optional;

import org.spec.research.open.xtrace.api.core.Trace;
import org.spec.research.open.xtrace.dflt.impl.core.SubTraceImpl;
import org.spec.research.open.xtrace.dflt.impl.core.TraceImpl;
import org.spec.research.open.xtrace.dflt.impl.core.callables.AbstractNestingCallableImpl;
import org.spec.research.open.xtrace.dflt.impl.core.callables.DatabaseInvocationImpl;
import org.spec.research.open.xtrace.dflt.impl.core.callables.MethodInvocationImpl;

public class TraceCreatorForTesting {

	private static final int MILLIS_TO_NANO = 1000000;

	public Trace createTrace() {
		TraceImpl trace = new TraceImpl();
		SubTraceImpl subtrace = new SubTraceImpl();
		trace.setRoot(subtrace);

		MethodInvocationImpl methodInvocationImpl = createMethodInvocation(
				15000, 11000, null);

		MethodInvocationImpl methodInvocationImpl2 = createMethodInvocation(
				1000, 0, methodInvocationImpl);

		MethodInvocationImpl methodInvocationImpl3 = createMethodInvocation(
				1000, 3000, methodInvocationImpl);
		
		MethodInvocationImpl methodInvocationImpl4 = createMethodInvocation(
				1000, 3000, methodInvocationImpl);
		
		MethodInvocationImpl methodInvocationImpl5 = createMethodInvocation(
				1000, 4000, methodInvocationImpl);


		subtrace.setRoot(methodInvocationImpl);

		createDatabaseInvocation(40, "SELECT id FROM table",
				methodInvocationImpl2);

		createDatabaseInvocation(42, "SELECT * FROM table",
				methodInvocationImpl3);
		
		createDatabaseInvocation(42, "SELECT * FROM table",
				methodInvocationImpl4);
		
		createDatabaseInvocation(42, "SELECT * FROM table",
				methodInvocationImpl5);

		return trace;
	}

	private DatabaseInvocationImpl createDatabaseInvocation(long timestamp,
			String sql, AbstractNestingCallableImpl nestingCallable) {
		DatabaseInvocationImpl databaseInvocationImpl = new DatabaseInvocationImpl(
				nestingCallable, null);
		databaseInvocationImpl.setSQLStatement(sql);
		databaseInvocationImpl.setTimestamp(timestamp);
		databaseInvocationImpl.setResponseTime(1000000000);

		return databaseInvocationImpl;
	}

	private MethodInvocationImpl createMethodInvocation(int responseTime,
			int cpuTime, AbstractNestingCallableImpl callable) {

		MethodInvocationImpl methodInvocationImpl = new MethodInvocationImpl(
				callable, null);
		methodInvocationImpl
				.setResponseTime(1l * responseTime * MILLIS_TO_NANO);
		methodInvocationImpl.setCPUTime(Optional.of(1l * cpuTime
				* MILLIS_TO_NANO));

		return methodInvocationImpl;
	}
}
