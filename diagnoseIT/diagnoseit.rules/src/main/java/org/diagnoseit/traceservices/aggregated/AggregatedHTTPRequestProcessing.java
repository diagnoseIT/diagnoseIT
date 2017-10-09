package org.diagnoseit.traceservices.aggregated;

import org.spec.research.open.xtrace.api.core.callables.HTTPMethod;
import org.spec.research.open.xtrace.api.core.callables.HTTPRequestProcessing;
import org.spec.research.open.xtrace.api.core.callables.TimedCallable;



public class AggregatedHTTPRequestProcessing extends AbstractAggregatedTimedCallable<HTTPRequestProcessing> {

	private HTTPMethod httpMethod;
	private String uri;

	public AggregatedHTTPRequestProcessing() {

	}

	public AggregatedHTTPRequestProcessing(boolean keepCallables) {
		super(keepCallables);
	}

	public AggregatedHTTPRequestProcessing(HTTPRequestProcessing httpRequest, boolean keepCallables) {
		super(httpRequest, keepCallables);
	}

	public HTTPMethod getRequestMethod() {
		return httpMethod;
	}

	public String getUri() {
		return uri;
	}

	@Override
	protected boolean canAggregate(HTTPRequestProcessing httpRequest) {
		if (getCount() > 0 && (!uri.equals(httpRequest.getUri()) || !httpMethod.equals(httpRequest.getRequestMethod()))) {
			return false;
		}
		return true;
	}

	@Override
	protected void add(HTTPRequestProcessing timedCallable) {
		if(uri == null){
			httpMethod = timedCallable.getRequestMethod().orElse(HTTPMethod.UNKNOWN);
			uri = timedCallable.getUri();
		}
	}

	@Override
	protected boolean canAggregate(AbstractAggregatedTimedCallable<? extends TimedCallable> other) {
		AggregatedHTTPRequestProcessing aggHTTPRequest = (AggregatedHTTPRequestProcessing) other;
		if (getCount() > 0 && (!uri.equals(aggHTTPRequest.uri) || !httpMethod.equals(aggHTTPRequest.httpMethod))) {
			return false;
		}
		return true;
	}

	@Override
	protected void add(AbstractAggregatedTimedCallable<? extends TimedCallable> other) {
		if (uri == null) {
			AggregatedHTTPRequestProcessing aggHTTPRequest = (AggregatedHTTPRequestProcessing) other;
			httpMethod = aggHTTPRequest.httpMethod;
			uri = aggHTTPRequest.uri;
		}

	}

}
