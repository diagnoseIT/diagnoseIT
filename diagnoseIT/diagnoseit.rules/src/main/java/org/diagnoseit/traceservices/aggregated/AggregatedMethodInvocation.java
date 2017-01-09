package org.diagnoseit.traceservices.aggregated;

import java.util.Collections;

import org.spec.research.open.xtrace.api.core.callables.MethodInvocation;
import org.spec.research.open.xtrace.api.core.callables.TimedCallable;



public class AggregatedMethodInvocation extends AbstractAggregatedTimedCallable<MethodInvocation> {

	private NumericStatistics<Integer> childCount;
	private NumericStatistics<Long> cpuTime;
	private NumericStatistics<Long> exclCPUTime;

	private Signature signature;

	private static final String EMPTY_STRING = "";

	public AggregatedMethodInvocation() {

	}

	public AggregatedMethodInvocation(boolean keepCallables) {
		super(keepCallables);
		childCount = new NumericStatistics<Integer>(Integer.class);
		cpuTime = new NumericStatistics<Long>(Long.class);
		exclCPUTime = new NumericStatistics<Long>(Long.class);
	}

	public AggregatedMethodInvocation(MethodInvocation methodInvocation, boolean keepCallables) {
		super(methodInvocation, keepCallables);
	}

	public int getChildCountAvg() {
		return childCount.getMean();
	}

	public int getChildCountSum() {
		return childCount.getSum();
	}

	public long getCPUTimeAvg() {
		return cpuTime.getMean();
	}

	public long getCPUTimeSum() {
		return cpuTime.getSum();
	}

	public long getExclusiveCPUTimeAvg() {
		return exclCPUTime.getMean();
	}

	public long getExclusiveCPUTimeSum() {
		return exclCPUTime.getSum();
	}

	public Signature getSignature() {
		return signature;
	}

	@Override
	protected boolean canAggregate(MethodInvocation methodInvocation) {
		if (getCount() > 0 && !methodInvocation.getSignature().equals(signature.getSignatureString())) {
			return false;
		} else {
			return true;
		}

	}

	@Override
	protected void add(MethodInvocation methodInvocation) {
		childCount.addValue(methodInvocation.getChildCount());
		methodInvocation.getCPUTime().ifPresent(v -> cpuTime.addValue(v));
		methodInvocation.getExclusiveCPUTime().ifPresent(v -> exclCPUTime.addValue(v));

		if (signature == null) {
			signature = new Signature(methodInvocation.getReturnType().orElse(EMPTY_STRING),
					methodInvocation.getPackageName().orElse(EMPTY_STRING),
					methodInvocation.getClassName().orElse(EMPTY_STRING),
					methodInvocation.getMethodName().orElse(EMPTY_STRING),
					methodInvocation.getParameterTypes().orElse(Collections.emptyList()));
		}
	}

	@Override
	protected boolean canAggregate(AbstractAggregatedTimedCallable<? extends TimedCallable> other) {
		AggregatedMethodInvocation aggMethodInvoc = (AggregatedMethodInvocation) other;
		if (getCount() > 0 && !aggMethodInvoc.signature.equals(signature)) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	protected void add(AbstractAggregatedTimedCallable<? extends TimedCallable> other) {
		AggregatedMethodInvocation aggMethodInvoc = (AggregatedMethodInvocation) other;
		childCount.mergeStatistics(aggMethodInvoc.childCount);
		cpuTime.mergeStatistics(aggMethodInvoc.cpuTime);
		exclCPUTime.mergeStatistics(aggMethodInvoc.exclCPUTime);
		if (signature == null) {
			signature = aggMethodInvoc.signature;
		}

	}

}
