package org.diagnoseit.traceservices.aggregated;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.spec.research.open.xtrace.api.core.AdditionalInformation;
import org.spec.research.open.xtrace.api.core.SubTrace;
import org.spec.research.open.xtrace.api.core.callables.NestingCallable;
import org.spec.research.open.xtrace.api.core.callables.TimedCallable;

public class BusinessTransactionCall implements TimedCallable {
	private TimedCallable coreTimedCallable;
	private String businessTransaction;

	public BusinessTransactionCall(TimedCallable coreTimedCallable) {
		this.coreTimedCallable = coreTimedCallable;
		if (coreTimedCallable != null) {
			setBusinessTransaction(coreTimedCallable.getContainingSubTrace().getLocation().getBusinessTransaction().orElse("unknown"));
		} else {
			businessTransaction = "unknown";
		}
	}

	@Override
	public Optional<Collection<AdditionalInformation>> getAdditionalInformation() {
		return coreTimedCallable.getAdditionalInformation();
	}

	@Override
	public <T extends AdditionalInformation> Optional<Collection<T>> getAdditionalInformation(Class<T> arg0) {
		return coreTimedCallable.getAdditionalInformation(arg0);
	}

	@Override
	public SubTrace getContainingSubTrace() {
		return coreTimedCallable.getContainingSubTrace();
	}

	@Override
	public Optional<List<String>> getLabels() {
		return coreTimedCallable.getLabels();
	}

	@Override
	public NestingCallable getParent() {
		return coreTimedCallable.getParent();
	}

	@Override
	public long getTimestamp() {
		return coreTimedCallable.getTimestamp();
	}

	@Override
	public Optional<Object> getIdentifier() {
		return coreTimedCallable.getIdentifier();
	}

	@Override
	public void setIdentifier(Object arg0) {
		coreTimedCallable.setIdentifier(arg0);

	}

	@Override
	public long getExclusiveTime() {
		return coreTimedCallable.getExclusiveTime();
	}

	@Override
	public long getResponseTime() {
		return coreTimedCallable.getResponseTime();
	}

	@Override
	public long getExitTime() {
		return coreTimedCallable.getExitTime();
	}

	/**
	 * @return the businessTransaction
	 */
	public String getBusinessTransaction() {
		return businessTransaction;
	}

	/**
	 * @param businessTransaction
	 *            the businessTransaction to set
	 */
	public void setBusinessTransaction(String businessTransaction) {
		this.businessTransaction = businessTransaction;
	}

	@Override
	public Optional<String> getThreadName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Long> getThreadID() {
		// TODO Auto-generated method stub
		return null;
	}

}
