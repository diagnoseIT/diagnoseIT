package org.diagnoseit.traceservices.aggregated;

import org.spec.research.open.xtrace.api.core.callables.TimedCallable;

public class AggregatedBusinessTransaction extends AbstractAggregatedTimedCallable<BusinessTransactionCall> {

	private String businessTransaction;

	public AggregatedBusinessTransaction() {
		super();
	}

	public AggregatedBusinessTransaction(boolean keepCallables) {
		super(keepCallables);
	}

	public AggregatedBusinessTransaction(BusinessTransactionCall businessTransactionCall, boolean keepCallables) {
		super(businessTransactionCall, keepCallables);
	}

	@Override
	protected boolean canAggregate(BusinessTransactionCall businessTransactionCall) {
		if (getCount() > 0 && !businessTransactionCall.getBusinessTransaction().equals(businessTransaction)) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	protected boolean canAggregate(AbstractAggregatedTimedCallable<? extends TimedCallable> other) {
		AggregatedBusinessTransaction aggBTCall = (AggregatedBusinessTransaction) other;
		if (getCount() > 0 && !aggBTCall.businessTransaction.equals(businessTransaction)) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	protected void add(BusinessTransactionCall businessTransactionCall) {
		if (businessTransaction == null) {
			businessTransaction = businessTransactionCall.getBusinessTransaction();
		}

	}

	@Override
	protected void add(AbstractAggregatedTimedCallable<? extends TimedCallable> other) {
		AggregatedBusinessTransaction aggBTCall = (AggregatedBusinessTransaction) other;
		if (businessTransaction == null) {
			businessTransaction = aggBTCall.getBusinessTransactionName();
		}

	}

	/**
	 * @return the businessTransaction
	 */
	public String getBusinessTransactionName() {
		return businessTransaction;
	}

}
