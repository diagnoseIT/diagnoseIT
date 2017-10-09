package org.diagnoseit.traceservices.aggregated;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import org.diagnoseit.traceservices.result.ProblemInstance;
import org.spec.research.open.xtrace.api.core.SubTrace;
import org.spec.research.open.xtrace.api.core.callables.DatabaseInvocation;
import org.spec.research.open.xtrace.api.core.callables.HTTPRequestProcessing;
import org.spec.research.open.xtrace.api.core.callables.MethodInvocation;
import org.spec.research.open.xtrace.api.core.callables.RemoteInvocation;
import org.spec.research.open.xtrace.api.core.callables.TimedCallable;

public abstract class AbstractAggregatedTimedCallable<T extends TimedCallable> {

	public static AbstractAggregatedTimedCallable<? extends TimedCallable> createAggregatedCallable(
			TimedCallable callable, boolean keepCallables) {
		if (callable instanceof MethodInvocation) {
			return new AggregatedMethodInvocation(keepCallables);
		} else if (callable instanceof HTTPRequestProcessing) {
			return new AggregatedHTTPRequestProcessing(keepCallables);
		} else if (callable instanceof DatabaseInvocation) {
			return new AggregatedDatabaseInvocation(keepCallables);
		} else if (callable instanceof RemoteInvocation) {
			return new AggregatedRemoteInvocation(keepCallables);
		} else if (callable instanceof BusinessTransactionCall) {
			return new AggregatedBusinessTransaction(keepCallables);
		} else {
			throw new RuntimeException("Unsupported Timed Callable type!");
		}
	}

	private List<T> timedCallables;
	private NumericStatistics<Long> responseTimeStats;
	private NumericStatistics<Long> exclusiveTimeStats;

	private TimedCallable globalContext;
	
	private ProblemInstance problemInstance;

	public AbstractAggregatedTimedCallable() {
		this(false);
	}

	public AbstractAggregatedTimedCallable(boolean keepCallables) {
		if (keepCallables) {
			timedCallables = new ArrayList<T>();
		}
		responseTimeStats = new NumericStatistics<Long>(Long.class);
		exclusiveTimeStats = new NumericStatistics<Long>(Long.class);
	}

	public AbstractAggregatedTimedCallable(T timedCallable) {
		this(timedCallable, true);
	}

	public AbstractAggregatedTimedCallable(T timedCallable,
			boolean keepCallables) {
		this(keepCallables);
		aggregate(timedCallable);
	}

	public boolean aggregate(
			AbstractAggregatedTimedCallable<? extends TimedCallable> other) {
		if (!getType().isAssignableFrom(other.getType())) {
			return false;
		}
		if (!canAggregate(other)) {
			return false;
		}
		if (timedCallables != null) {
			for (TimedCallable tCallable : other.getCallables()) {
				timedCallables.add((T) tCallable);
			}
		}
		getResponseTimeStats().mergeStatistics(other.getResponseTimeStats());
		getExclusiveTimeStats().mergeStatistics(other.getExclusiveTimeStats());
		add(other);
		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean aggregate(TimedCallable timedCallable) {

		if (!getType().isAssignableFrom(timedCallable.getClass())) {
			return false;
		}

		T tCallable = (T) timedCallable;

		if (!canAggregate(tCallable)) {
			return false;
		}
		// if (representative == null) {
		// representative = tCallable;
		// }
		if (timedCallables != null) {
			timedCallables.add(tCallable);
		}

		getResponseTimeStats().addValue(timedCallable.getResponseTime() / 1000000);
		getExclusiveTimeStats().addValue(timedCallable.getExclusiveTime() / 1000000);
		add(tCallable);
		return true;
	}

	protected abstract boolean canAggregate(T callable);

	protected abstract boolean canAggregate(
			AbstractAggregatedTimedCallable<? extends TimedCallable> other);

	protected abstract void add(T timedCallable);

	protected abstract void add(
			AbstractAggregatedTimedCallable<? extends TimedCallable> other);

	public SubTrace getContainingSubTrace() {
		if (timedCallables != null && !timedCallables.isEmpty()) {
			return timedCallables.get(0).getContainingSubTrace();
		}
		return null;
	}

	public int getCount() {
		return getResponseTimeStats().getCount();
	}

	public List<T> getCallables() {
		return timedCallables;
	}

	public Class<?> getType() {
		ParameterizedType parameterizedType = (ParameterizedType) getClass()
				.getGenericSuperclass();
		return (Class<?>) parameterizedType.getActualTypeArguments()[0];
	}

	/**
	 * @return the responseTimeStats
	 */
	public NumericStatistics<Long> getResponseTimeStats() {
		return responseTimeStats;
	}

	/**
	 * @return the exclusiveTimeStats
	 */
	public NumericStatistics<Long> getExclusiveTimeStats() {
		return exclusiveTimeStats;
	}

	public void setGlobalContext(TimedCallable globalContext) {
		this.globalContext = globalContext;
	}

	public TimedCallable getGlobalContext() {
		return this.globalContext;
	}

	public ProblemInstance getProblemInstance() {
		return problemInstance;
	}

	public void setProblemInstance(ProblemInstance problemInstance) {
		this.problemInstance = problemInstance;
	}

}
