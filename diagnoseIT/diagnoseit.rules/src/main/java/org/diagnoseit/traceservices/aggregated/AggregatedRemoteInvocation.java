package org.diagnoseit.traceservices.aggregated;

import org.spec.research.open.xtrace.api.core.callables.RemoteInvocation;
import org.spec.research.open.xtrace.api.core.callables.TimedCallable;

public class AggregatedRemoteInvocation extends AbstractAggregatedTimedCallable<RemoteInvocation> {

	public AggregatedRemoteInvocation() {

	}

	public AggregatedRemoteInvocation(boolean keepCallables) {
		super(keepCallables);
	}

	public AggregatedRemoteInvocation(RemoteInvocation timedCallable, boolean keepCallables) {
		super(timedCallable, keepCallables);
	}

	@Override
	protected boolean canAggregate(RemoteInvocation callable) {
		return getCount() == 0;
	}

	@Override
	protected void add(RemoteInvocation timedCallable) {
		// nothing to do here
	}

	@Override
	protected boolean canAggregate(AbstractAggregatedTimedCallable<? extends TimedCallable> other) {
		return getCount() == 0;
	}

	@Override
	protected void add(AbstractAggregatedTimedCallable<? extends TimedCallable> other) {
		// TODO Auto-generated method stub
		
	}

}
